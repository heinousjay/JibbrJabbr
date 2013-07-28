package jj.http.server;

import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DateFormatHelper;
import jj.http.AbstractHttpRequest;
import jj.http.HttpRequest;


import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

@Singleton
class JJHttpServerRequest extends AbstractHttpRequest implements HttpRequest {
	
	protected final Channel channel;
	
	@Inject
	JJHttpServerRequest(final FullHttpRequest request, final Channel channel) {
		super(request);
		this.channel = channel;
	}

	/**
	 * @return
	 */
	public SocketAddress remoteAddress() {
		return channel.remoteAddress();
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
			DateFormatHelper.basicFormat(timestamp()) +
			" with data " +
			data;
	}
}
