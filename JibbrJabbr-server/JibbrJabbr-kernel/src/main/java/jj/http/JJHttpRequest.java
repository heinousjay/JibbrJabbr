package jj.http;

import java.math.BigDecimal;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DataStore;
import jj.DateFormatHelper;
import jj.Sequence;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

@Singleton
public class JJHttpRequest implements DataStore {
	
	public enum State {
		Uninitialized,
		InitialExecution {
			@Override
			public String toString() {
				return "Initial execution";
			}
		},
		ReadyFunctionExecution {
			@Override
			public String toString() {
				return "Ready function execution";
			}
		};
	}
	
	private static final Sequence sequence = new Sequence();
	
	private static final String HEADER_X_HOST = "x-host";
	
	private static final String HEADER_X_SECURE = "x-secure";
	
	protected final String id = sequence.next();
	
	private final FullHttpRequest request;
	
	protected final Channel channel;
	
	protected final HashMap<String, Object> data = new HashMap<>();
	
	protected final long startTime = System.nanoTime();
	
	protected State state = State.Uninitialized;
	
	protected AssociatedScriptBundle associatedScriptBundle;
	
	private ArrayList<JQueryMessage> messages;
	
	@Inject
	public JJHttpRequest(final FullHttpRequest request, final Channel channel) {
		this.request = request;
		this.channel = channel;
	}

	@Override
	public JJHttpRequest data(String name, Object value) {
		data.put(name, value);
		return this;
	}
	
	@Override
	public Object data(String name) {
		return data.get(name);
	}
	
	@Override
	public boolean containsData(String name) {
		return data.containsKey(name);
	}
	
	@Override
	public Object removeData(String name) {
		return data.remove(name);
	}

	public BigDecimal wallTime() {
		return BigDecimal.valueOf(System.nanoTime() - startTime, 6);
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return associatedScriptBundle;
	}
	
	public JJHttpRequest associatedScriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		this.associatedScriptBundle = associatedScriptBundle;
		return this;
	}
	
	public JJHttpRequest startingInitialExecution() {
		state = State.InitialExecution;
		associatedScriptBundle().initializing(true);
		return this;
	}
	
	public JJHttpRequest startingReadyFunction() {
		state = State.ReadyFunctionExecution;
		return this;
	}
	
	public State state() {
		return state;
	}
	
	public String host() {
		String xHost = header(HEADER_X_HOST);
		String host = header(HttpHeaders.Names.HOST);
		return xHost == null ? host : xHost;
	}
	
	public boolean secure() {
		return "true".equals(header(HEADER_X_SECURE));
	}
	
	public URI absoluteUri() {
		return URI.create(
			new StringBuilder("http")
				.append(secure() ? "s" : "")
				.append("://")
				.append(host())
				.append(uri())
				.toString()
		);
	}

	/**
	 * adds a message intended to be processed a framework startup
	 * on the client.  initially intended for event bindings but
	 * some other case may come up
	 * @param message
	 */
	public JJHttpRequest addStartupJQueryMessage(final JQueryMessage message) {
		if (messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(message);
		return this;
	}
	
	public List<JQueryMessage> startupJQueryMessages() {
		ArrayList<JQueryMessage> messages = this.messages;
		this.messages = null;
		return messages == null ? Collections.<JQueryMessage>emptyList() : messages;
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

	/**
	 * @return
	 */
	public long timestamp() {
		return startTime;
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
	public String uri() {
		return request.getUri();
	}

	/**
	 * @param ifNoneMatch
	 * @return
	 */
	public boolean hasHeader(String ifNoneMatch) {
		return request.headers().contains(ifNoneMatch);
	}

	/**
	 * @param etag
	 * @return
	 */
	public String header(String name) {
		return request.headers().get(name);
	}

	/**
	 * @return
	 */
	public String id() {
		return id;
	}

	/**
	 * @return
	 */
	public String body() {
		return request.content().toString(charset());
	}

	/**
	 * @return
	 */
	public Charset charset() {
		// TODO figure this out some day
		return StandardCharsets.UTF_8;
	}

	/**
	 * @return
	 */
	public HttpMethod method() {
		return request.getMethod();
	}

	/**
	 * @return
	 */
	public List<Entry<String, String>> allHeaders() {
		return request.headers().entries();
	}

	/**
	 * @param userAgent
	 * @param userAgent2
	 */
	public JJHttpRequest header(String name, String value) {
		request.headers().add(name, value);
		return this;
	}

	/**
	 * @param string
	 */
	public JJHttpRequest method(HttpMethod method) {
		request.setMethod(method);
		return this;
	}

	/**
	 * @param uri
	 */
	public JJHttpRequest uri(String uri) {
		request.setUri(uri);
		return this;
	}

	/**
	 * @return
	 */
	protected FullHttpRequest request() {
		return request;
	}
}
