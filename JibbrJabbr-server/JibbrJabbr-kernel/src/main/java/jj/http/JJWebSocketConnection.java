package jj.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DataStore;
import jj.DateFormatHelper;
import jj.ExecutionTrace;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JJWebSocketConnection implements DataStore {
	
	private static final String CLIENT_STORAGE = "client storage";
	
	private final Logger log = LoggerFactory.getLogger(JJWebSocketConnection.class);
	
	private final ExecutionTrace trace;
	
	private final String uri;
	
	private final Channel channel;
	
	private final AssociatedScriptBundle scriptBundle;
	
	private final HashMap<String, Object> data = new HashMap<>();
	
	// room for four messages initially should be good
	private final List<JQueryMessage> messages = new ArrayList<>(4);
	
	// accessed from many threads
	private volatile long lastActivity = System.currentTimeMillis();
	
	private final String description;

	@Inject
	JJWebSocketConnection(
		final ExecutionTrace trace,
		final FullHttpRequest request,
		final Channel channel,
		final AssociatedScriptBundle scriptBundle
	) {
		this.trace = trace;
		this.uri = request.getUri();
		this.channel = channel;
		this.scriptBundle = scriptBundle;
		description = String.format(
			"WebSocket connection to %s started at %s",
			channel.remoteAddress(),
			DateFormatHelper.nowInBasicFormat()
		);
	}
	
	@Override
	public JJWebSocketConnection data(String name, Object value) {
		data.put(name, value);
		return this;
	}
	
	@Override
	public Object data(String name) {
		return data.get(name);
	}
	
	@Override
	public Object removeData(String name) {
		return data.remove(name);
	}
	
	@Override
	public boolean containsData(String name) {
		return data.containsKey(name);
	}
	
	void markActivity() {
		lastActivity = System.currentTimeMillis();
	}
	
	long lastActivity() {
		return lastActivity;
	}
	
	public String baseName() {
		return associatedScriptBundle().baseName();
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return scriptBundle;
	}
	
	public Map<String, Object> clientStorage() {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)data.get(CLIENT_STORAGE);
		if (map == null) {
			map = new HashMap<>();
			data.put(CLIENT_STORAGE, map);
		}
		return map;
	}
	
	public JJWebSocketConnection send(JQueryMessage message) {
		messages.add(message);
		return this;
	}
	
	public JJWebSocketConnection send(String message) {
		markActivity();
		log.trace("sending {} on {}", message, this);
		channel.write(new TextWebSocketFrame(message));
		return this;
	}
	
	public void end() {
		if (!messages.isEmpty()) {
			markActivity();
			String message = serialize();
			trace.send(this, message);
			channel.write(new TextWebSocketFrame(message));
		}
	}
	
	private String serialize() {
		StringBuilder output = new StringBuilder();
		if (!messages.isEmpty()) {
			output.append("[");
			for (JQueryMessage message : messages) {
				output.append(message).append(',');
			}
			output.setCharAt(output.length() - 1, ']');
			messages.clear();
		}
		return output.toString();
	}
	
	@Override
	public String toString() {
		return description;
	}

	/**
	 * @return
	 */
	public String uri() {
		return uri;
	}

	/**
	 * 
	 */
	public void close() {
		channel.write(new CloseWebSocketFrame(1000, null)).addListener(ChannelFutureListener.CLOSE);
	}
}
