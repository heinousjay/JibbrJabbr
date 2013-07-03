package jj.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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
	
	private final WebSocketConnectionMaker webSocketConnectionMaker;
	
	@Inject
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final Set<Servable> resourceTypes,
		final Injector parentInjector,
		final ExecutionTrace trace,
		final WebSocketConnectionMaker webSocketConnectionMaker
	) {
		this.executors = executors;
		this.resourceTypes = resourceTypes;
		this.parentInjector = parentInjector;
		this.trace = trace;
		this.webSocketConnectionMaker = webSocketConnectionMaker;
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
		
		} else if (webSocketConnectionMaker.isWebSocketRequest(request)) {
		
			webSocketConnectionMaker.handshakeWebsocket(ctx, request);
			
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
		executors.ioExecutor().execute(executors.prepareTask(new JJRunnable("JJEngine core processing") {
			
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
