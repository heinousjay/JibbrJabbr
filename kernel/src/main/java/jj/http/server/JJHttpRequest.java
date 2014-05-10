package jj.http.server;

import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.uri.RouteFinder;
import jj.util.DateFormatHelper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

@Singleton
class JJHttpRequest extends AbstractHttpRequest {
	
	protected final ChannelHandlerContext ctx;
	
	@Inject
	JJHttpRequest(final FullHttpRequest request, final RouteFinder routeFinder, final ChannelHandlerContext ctx) {
		super(request, routeFinder);
		this.ctx = ctx;
	}

	/**
	 * @return
	 */
	public SocketAddress remoteAddress() {
		return ctx.channel().remoteAddress();
	}

	/**
	 * @return
	 */
	protected FullHttpRequest request() {
		return request;
	}

	@Override
	public String toString() {
		return "httpRequest[" +
			remoteAddress() +
			"] started at " +
			DateFormatHelper.basicFormat(timestamp());
	}
}
