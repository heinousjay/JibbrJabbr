package jj.http.server;

import java.math.BigDecimal;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.uri.URIMatch;
import jj.util.Sequence;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AsciiString;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;

@Singleton
class HttpServerRequestImpl implements HttpServerRequest {
	
	private static final Sequence sequence = new Sequence();
	
	private static final AsciiString HEADER_X_HOST = new AsciiString("X-Host");
	
	private static final AsciiString HEADER_X_FORWARDED_PROTO = new AsciiString("X-Forwarded-Proto");
	
	private final long startTime = System.nanoTime();
	
	private final String id = sequence.next();
	
	private final FullHttpRequest request;
	
	private final URIMatch uriMatch;
	
	private final ChannelHandlerContext ctx;
	
	@Inject
	HttpServerRequestImpl(final FullHttpRequest request, final ChannelHandlerContext ctx) {
		this.request = request;
		this.uriMatch = new URIMatch(request.uri());
		this.ctx = ctx;
	}
	

	@Override
	public BigDecimal wallTime() {
		return BigDecimal.valueOf(System.nanoTime() - startTime, 6);
	}
	
	@Override
	public String id() {
		return id;
	}

	@Override
	public String host() {
		CharSequence xHost = header(HEADER_X_HOST);
		CharSequence host = header(HttpHeaderNames.HOST);
		return (xHost == null ? host : xHost).toString();
	}

	@Override
	public boolean secure() {
		return "https".equals(header(HEADER_X_FORWARDED_PROTO));
	}

	@Override
	public URI absoluteUri() {
		return URI.create(
			new StringBuilder("http")
				.append(secure() ? "s" : "")
				.append("://")
				.append(host())
				.append(uriMatch().uri)
				.toString()
		);
	}

	/**
	 * @return
	 */
	@Override
	public long timestamp() {
		return startTime;
	}

	/**
	 * @return
	 */
	@Override
	public Charset charset() {
		return StandardCharsets.UTF_8;
	}
	
	public URIMatch uriMatch() {
		return uriMatch;
	}

	/**
	 * @param ifNoneMatch
	 * @return
	 */
	@Override
	public boolean hasHeader(AsciiString ifNoneMatch) {
		return request.headers().contains(ifNoneMatch);
	}

	/**
	 * @param etag
	 * @return
	 */
	@Override
	public CharSequence header(AsciiString name) {
		return request.headers().get(name);
	}

	/**
	 * @return
	 */
	@Override
	public String body() {
		return request.content().toString(charset());
	}

	/**
	 * @return
	 */
	@Override
	public HttpMethod method() {
		return request.method();
	}

	/**
	 * @return
	 */
	@Override
	public Iterable<Entry<CharSequence, CharSequence>> allHeaders() {
		return request.headers();
	}
	
	@Override
	public Locale locale() {
		// TODO make this not hard-coded! haha
		return Locale.US;
	}

	@Override
	public Cookie cookie(String name) {
		return null;
	}
	
	@Override
	public List<Cookie> cookies() {
		// TODO Auto-generated method stub
		return null;
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
		return request.toString();
	}
}
