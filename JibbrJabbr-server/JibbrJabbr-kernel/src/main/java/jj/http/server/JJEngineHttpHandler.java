package jj.http.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import jj.execution.ExecutionTrace;
import jj.execution.JJExecutors;
import jj.execution.JJRunnable;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.http.server.servable.RequestProcessor;
import jj.http.server.servable.Servable;
import jj.logging.EmergencyLogger;

/**
 * Acts as the bridge from netty into our core.
 * @author jason
 *
 */
public class JJEngineHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private final JJExecutors executors;
	
	private final Set<Servable> resourceTypes;
	
	private final Injector parentInjector;
	
	private final ExecutionTrace trace;
	
	private final WebSocketConnectionMaker webSocketConnectionMaker;
	
	private final Logger logger;
	
	@Inject
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final Set<Servable> resourceTypes,
		final Injector parentInjector,
		final ExecutionTrace trace,
		final WebSocketConnectionMaker webSocketConnectionMaker,
		final @EmergencyLogger Logger logger
	) {
		this.executors = executors;
		this.resourceTypes = resourceTypes;
		this.parentInjector = parentInjector;
		this.trace = trace;
		this.webSocketConnectionMaker = webSocketConnectionMaker;
		this.logger = logger;
	}
	
	private Servable[] findMatchingServables(final HttpRequest request) {
		
		List<Servable> result = new ArrayList<>();
		
		for (final Servable type : resourceTypes) {
			if (type.isMatchingRequest(request)) {
				result.add(type);
			}
		}
		
		return result.toArray(new Servable[result.size()]);
	}
	
	

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
		
		Injector injector = parentInjector.createChildInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Channel.class).toInstance(ctx.channel());
				bind(FullHttpRequest.class).toInstance(request);
				bind(HttpRequest.class).to(JJHttpServerRequest.class);
				bind(HttpResponse.class).to(JJHttpServerResponse.class);
			}
		});
		
		if (!request.getDecoderResult().isSuccess()) {
		
			injector.getInstance(HttpResponse.class).sendError(HttpResponseStatus.BAD_REQUEST);
		
		} else if (webSocketConnectionMaker.isWebSocketRequest(request)) {
		
			webSocketConnectionMaker.handshakeWebsocket(ctx, request);
			
		} else {
			
			handleHttpRequest(injector.getInstance(HttpRequest.class), injector.getInstance(HttpResponse.class));
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (!(cause instanceof IOException)) {
			logger.error("engine caught an exception", cause);
			try {
				ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR))
					.addListener(ChannelFutureListener.CLOSE);
			} catch (Exception e) {
				logger.error("additionally, an exception occurred while responding with an error", e);
			}
		}
	}

	void handleHttpRequest(
		final HttpRequest request,
		final HttpResponse response
	) throws Exception {
		
		trace.start(request, response);
		
		// figure out if there's something for us to do
		final Servable[] servables = findMatchingServables(request);
		
		assert (servables.length > 0) : "no servables found - something is misconfigured";
		executors.ioExecutor().submit(new JJRunnable("JJEngine core processing") {
			@Override
			public void run() {
				try {
					boolean found = false;
					for (Servable servable : servables) {
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
