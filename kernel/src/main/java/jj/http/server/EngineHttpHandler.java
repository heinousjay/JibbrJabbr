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
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

import jj.execution.TaskRunner;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.RequestProcessor;
import jj.http.server.servable.Servable;
import jj.http.server.servable.Servables;
import jj.logging.EmergencyLogger;
import jj.resource.ResourceTask;
import jj.resource.Resource;

/**
 * Acts as the bridge from netty into our core.
 * @author jason
 * 
 * TODO this needs to be made into the new style without channelRead0 as a method name.  i think that's a thing
 *
 */
public class EngineHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private static final Pattern HTTP_REPLACER = Pattern.compile("http");
	
	private final TaskRunner taskRunner;
	
	private final Servables servables;
	
	private final Injector parentInjector;
	
	private final WebSocketRequestChecker webSocketRequestChecker;
	
	private final Logger logger;
	
	@Inject
	EngineHttpHandler( 
		final TaskRunner taskRunner,
		final Servables servables,
		final Injector parentInjector,
		final WebSocketRequestChecker webSocketRequestChecker,
		final @EmergencyLogger Logger logger
	) {
		this.taskRunner = taskRunner;
		this.servables = servables;
		this.parentInjector = parentInjector;
		this.webSocketRequestChecker = webSocketRequestChecker;
		this.logger = logger;
	}

	// TODO 
	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
		
		Injector injector = parentInjector.createChildInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(ChannelHandlerContext.class).toInstance(ctx);
				bind(FullHttpRequest.class).toInstance(request);
				bind(HttpRequest.class).to(JJHttpServerRequest.class);
				bind(HttpResponse.class).to(JJHttpServerResponse.class);
				bind(WebSocketConnectionMaker.class);
				bind(WebSocketFrameHandlerCreator.class);
			}
			
			@Provides
			protected WebSocketServerHandshakerFactory provideHandshaker() {
				String uri = HTTP_REPLACER.matcher(
					request.headers().get(HttpHeaders.Names.ORIGIN) + 
					request.getUri()
				).replaceFirst("ws");
				
				return new WebSocketServerHandshakerFactory(uri, null, false);
			}
		});
		
		if (!request.getDecoderResult().isSuccess()) {
		
			injector.getInstance(HttpResponse.class).sendError(HttpResponseStatus.BAD_REQUEST);
		
		} else if (webSocketRequestChecker.isWebSocketRequest(request)) {
			
			injector.getInstance(WebSocketConnectionMaker.class).handshakeWebsocket();
			
		} else {
			
			handleHttpRequest(injector.getInstance(HttpRequest.class), injector.getInstance(HttpResponse.class));
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (!(cause instanceof IOException)) {
			logger.error("engine caught an exception", cause);
			try {
				ctx.writeAndFlush(
					new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
				).addListener(ChannelFutureListener.CLOSE);
			} catch (Exception e) {
				logger.error("additionally, an exception occurred while responding with an error", e);
			}
		}
	}

	void handleHttpRequest(
		final HttpRequest request,
		final HttpResponse response
	) throws Exception {
		
		// look up the candidate ways to service the request
		// try them out to see if the request can be handled?
		//  - TODO always in the IO thread? not sure there, maybe document servable can launch the script execution immediately
		//  - but it may not matter since all threads will generally be warm under load and it's no big deal otherwise
		// see if the request can get handled
		// return 404 if not
		final List<Servable<? extends Resource>> list = servables.findMatchingServables(request.uriMatch());
		
		assert (!list.isEmpty()) : "no servables found - something is misconfigured";
		taskRunner.execute(new ResourceTask("JJEngine core processing") {
			@Override
			public void run() {
				try {
					boolean found = false;
					for (Servable<? extends Resource> servable : list) {
						RequestProcessor requestProcessor = servable.makeRequestProcessor(request, response);
						if (requestProcessor != null) {
							requestProcessor.process();
							found = true;
							break;
						}
					}
					if (!found) {
						response.sendNotFound();
					}
				} catch (Throwable e) {
					response.error(e);
				}
			}
		});
	}
}
