package jj.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.PlatformDependent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.DataStore;
import jj.DateFormatHelper;
import jj.ExecutionTrace;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

@Singleton
public class JJWebSocketConnection implements DataStore {
	
	private static final String CLIENT_STORAGE = "JJWebSocketConnection client storage";
	
	private final ExecutionTrace trace;
	
	private final Channel channel;
	
	private final AssociatedScriptBundle scriptBundle;
	
	private final ConcurrentMap<String, Object> data = PlatformDependent.<String, Object>newConcurrentHashMap();
	
	// room for four messages initially should be good
	private final List<JQueryMessage> messages = new ArrayList<>(4);
	
	private final String description;
	
	// accessed from many threads
	private volatile long lastActivity = System.currentTimeMillis();

	@Inject
	JJWebSocketConnection(
		final ExecutionTrace trace,
		final Channel channel,
		final AssociatedScriptBundle scriptBundle
	) {
		this.trace = trace;
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
		// TODO move this to something more independent so it can be picked up and put down
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
		trace.send(this, message);
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
	 * 
	 */
	public void close() {
		channel.write(new CloseWebSocketFrame(1000, null)).addListener(ChannelFutureListener.CLOSE);
	}
}
