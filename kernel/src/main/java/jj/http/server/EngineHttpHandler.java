package jj.http.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

import jj.event.Publisher;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.websocket.WebSocketConnectionMaker;
import jj.http.server.websocket.WebSocketFrameHandlerCreator;
import jj.http.server.websocket.WebSocketRequestChecker;
import jj.logging.Emergency;

/**
 * Reads incoming http messages and looks for ways to respond
 * @author jason
 * 
 *
 */
public class EngineHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private static final Pattern HTTP_REPLACER = Pattern.compile("http");
	
	private final ServableResources servables;
	
	private final Router router;
	
	private final Injector parentInjector;
	
	private final WebSocketRequestChecker webSocketRequestChecker;
	
	private final Publisher publisher;
	
	@Inject
	EngineHttpHandler(
		final ServableResources servables,
		final Router router,
		final Injector parentInjector,
		final WebSocketRequestChecker webSocketRequestChecker,
		final Publisher publisher
	) {
		this.servables = servables;
		this.router = router;
		this.parentInjector = parentInjector;
		this.webSocketRequestChecker = webSocketRequestChecker;
		this.publisher = publisher;
	}
	
	private Module makeRequestResponseModule(final ChannelHandlerContext ctx, final FullHttpRequest request) {
		return new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(ChannelHandlerContext.class).toInstance(ctx);
				bind(FullHttpRequest.class).toInstance(request);
				bind(HttpServerRequest.class).to(HttpServerRequestImpl.class);
				bind(HttpServerResponse.class).to(HttpServerResponseImpl.class);
			}
		};
	}
	
	private Module makeWebSocketHandshakerModule(final FullHttpRequest request) {
		return new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(WebSocketConnectionMaker.class);
				bind(WebSocketFrameHandlerCreator.class);
			}
			
			@Provides
			protected WebSocketServerHandshakerFactory provideHandshaker() {
				String uri = HTTP_REPLACER.matcher(
					request.headers().get(HttpHeaders.Names.ORIGIN) + 
					request.uri()
				).replaceFirst("ws");
				
				return new WebSocketServerHandshakerFactory(uri, null, false);
			}
		};
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
		
		//long time = System.nanoTime();
		// injector creation is split apart here because it's measurably slower to include the websocket bindings
		if (!request.decoderResult().isSuccess()) {
		
			Injector injector = parentInjector.createChildInjector(makeRequestResponseModule(ctx, request));
			//System.out.printf("made req/res injector in %s millis%n", MILLISECONDS.convert(System.nanoTime() - time, NANOSECONDS));
			
			injector.getInstance(HttpServerResponse.class).sendError(HttpResponseStatus.BAD_REQUEST);
		
		} else if (webSocketRequestChecker.isWebSocketRequest(request)) {
			
			Injector injector = parentInjector.createChildInjector(makeRequestResponseModule(ctx, request), makeWebSocketHandshakerModule(request));
			//System.out.printf("made websocket handshake injector in %s millis%n", MILLISECONDS.convert(System.nanoTime() - time, NANOSECONDS));
			
			injector.getInstance(WebSocketConnectionMaker.class).handshakeWebsocket();
			
		} else {
		
			Injector injector = parentInjector.createChildInjector(makeRequestResponseModule(ctx, request));
			//System.out.printf("made req/res injector in %s millis%n", MILLISECONDS.convert(System.nanoTime() - time, NANOSECONDS));
			
			handleHttpRequest(injector.getInstance(HttpServerRequest.class), injector.getInstance(HttpServerResponse.class));
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (!(cause instanceof IOException)) {
			publisher.publish(new Emergency("engine caught an exception", cause));
			try {
				ctx.writeAndFlush(
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
				).addListener(ChannelFutureListener.CLOSE);
			} catch (Exception e) {
				publisher.publish(new Emergency("additionally, an exception occurred while responding with an error", e));
			}
		}
	}

	private void handleHttpRequest(final HttpServerRequest request, final HttpServerResponse response) throws Exception {
		
		RouteMatch routeMatch = router.routeRequest(request.method(), request.uriMatch());
		RouteProcessor rp = servables.routeProcessor(routeMatch.resourceName());
		if (rp != null) {
			rp.process(routeMatch.route(), request, response);
		} else {
			response.sendNotFound();
		}
	}
}
