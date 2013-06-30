package jj.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.DateFormatHelper;
import jj.ExecutionTrace;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.logging.AccessLogger;
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
	
	private final Logger access;
	
	private final ExecutionTrace trace;
	
	@Inject
	JJEngineHttpHandler( 
		final JJExecutors executors,
		final Set<Servable> resourceTypes,
		final @AccessLogger Logger access, 
		final ExecutionTrace trace
	) {
		this.executors = executors;
		this.resourceTypes = resourceTypes;
		this.access = access;
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
	
	private void handshakeWebsocket(final ChannelHandlerContext ctx, final FullHttpRequest msg) {
		String uri = HTTP_REPLACER.matcher(msg.headers().get(HttpHeaders.Names.ORIGIN) + msg.getUri()).replaceFirst("ws");
		
		System.out.println(uri);
	}

	@Override
	protected void messageReceived(final ChannelHandlerContext ctx, final FullHttpRequest msg) throws Exception {
		JJHttpRequest request = new JJHttpRequest(msg, ctx.channel());
		JJHttpResponse response = new JJHttpResponse(request, ctx.channel(), access);
		
		if (!msg.getDecoderResult().isSuccess()) {
			response.sendError(HttpResponseStatus.BAD_REQUEST);
		} else if (msg.getUri().endsWith(".socket")) {
			handshakeWebsocket(ctx, msg);
		} else {
			handleHttpRequest(request, response);
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
