package jj.http;

import static jj.http.HttpServerChannelInitializer.PipelineStages.*;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.ExecutionTrace;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.servable.Servable;

/**
 * Acts as the bridge from netty into our core.
 * @author jason
 *
 */
public class JJEngineHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private static final Logger logger = LoggerFactory.getLogger(JJEngineHttpHandler.class);
	
	private final JJExecutors executors;
	
	private final Set<Servable> resourceTypes;
	
	private final Injector parentInjector;
	
	private final ExecutionTrace trace;
	
	@Inject
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final Set<Servable> resourceTypes,
		final Injector parentInjector,
		final ExecutionTrace trace
	) {
		this.executors = executors;
		this.resourceTypes = resourceTypes;
		this.parentInjector = parentInjector;
		this.trace = trace;
	}
	
	private Servable[] findMatchingServables(final JJHttpRequest request) {
		
		List<Servable> result = new ArrayList<>();
		
		for (final Servable type : resourceTypes) {
			if (type.isMatchingRequest(request)) {
				result.add(type);
			}
		}
		
		return result.toArray(new Servable[result.size()]);
	}
	
	private static final Pattern HTTP_REPLACER = Pattern.compile("http");
	
	private void handshakeWebsocket(final ChannelHandlerContext ctx, final FullHttpRequest request) {
		final String uri = 
			HTTP_REPLACER.matcher(request.headers().get(HttpHeaders.Names.ORIGIN) + request.getUri()).replaceFirst("ws");
		
		WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(uri, null, false);
		final WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		} else {
			handshaker.handshake(ctx.channel(), request).addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						Injector injector = parentInjector.createChildInjector(new AbstractModule() {
							@Override
							protected void configure() {
								bind(JJWebSocketConnection.class);
								bind(Channel.class).toInstance(ctx.channel());
								bind(FullHttpRequest.class).toInstance(request);
								bind(WebSocketServerHandshaker.class).toInstance(handshaker);
								bind(WebSocketFrameHandler.class);
							}
						});
						
						ctx.pipeline()
							.replace(JJEngineHttpHandler.this, JJWebsocketHandler.toString(), injector.getInstance(WebSocketFrameHandler.class))
							.remove(Compressor.toString());
					} else {
						ctx.channel().close();
					}
				}
			});
		}
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
		
		Injector injector = parentInjector.createChildInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Channel.class).toInstance(ctx.channel());
				bind(FullHttpRequest.class).toInstance(request);
				bind(JJHttpRequest.class);
				bind(JJHttpResponse.class);
			}
		});
		
		if (!request.getDecoderResult().isSuccess()) {
		
			injector.getInstance(JJHttpResponse.class).sendError(HttpResponseStatus.BAD_REQUEST);
		
		} else if (request.getUri().endsWith(".socket")) {
		
			handshakeWebsocket(ctx, request);
			
		} else {
			
			handleHttpRequest(injector.getInstance(JJHttpRequest.class), injector.getInstance(JJHttpResponse.class));
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("engine caught an exception", cause);
		ctx.channel().write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
		ctx.close();
	}

	void handleHttpRequest(
		final JJHttpRequest request,
		final JJHttpResponse response
	) throws Exception {
		
		trace.start(request, response);
		
		// figure out if there's something for us to do
		final Servable[] servables = findMatchingServables(request);
		
		if (servables.length > 0) {
			dispatchNextServable(request, response, servables, new AtomicInteger());
			
		} else {
			response.sendNotFound();
		}
	}
	
	private void dispatchNextServable(
		final JJHttpRequest request,
		final JJHttpResponse response,
		final Servable[] servables,
		final AtomicInteger count
	) {
		executors.ioExecutor().execute(executors.prepareTask(new JJRunnable("JJEngine webbit->core processing") {
			
			@Override
			public void run() throws Exception {
				try {
					RequestProcessor requestProcessor = 
						servables[count.getAndIncrement()].makeRequestProcessor(
							request,
							response
						);
					
					if (requestProcessor != null) {
						requestProcessor.process();
					} else if (count.get() < servables.length) {
						dispatchNextServable(request, response, servables, count);
					} else {
						response.sendNotFound();
					}
					
				} catch (Throwable e) {
					response.error(e);
				}
			}
		}));
	}
}
