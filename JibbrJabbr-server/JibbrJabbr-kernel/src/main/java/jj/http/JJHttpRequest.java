package jj.http;

import java.net.SocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;


import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

@Singleton
public class JJHttpRequest extends AbstractHttpRequest implements HttpRequest {
	
	protected final Channel channel;
	
	@Inject
	JJHttpRequest(final FullHttpRequest request, final Channel channel) {
		super(request);
		this.channel = channel;
	}

	/**
	 * @return
	 */
	@Override
	public SocketAddress remoteAddress() {
		return channel.remoteAddress();
	}

	/**
	 * @return
	 */
	protected FullHttpRequest request() {
		return request;
	}
}
