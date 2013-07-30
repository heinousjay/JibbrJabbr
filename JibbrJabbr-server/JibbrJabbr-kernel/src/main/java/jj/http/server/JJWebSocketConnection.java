package jj.http.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Callable;

import jj.DataStore;
import jj.DateFormatHelper;
import jj.execution.ExecutionTrace;
import jj.jjmessage.JJMessage;
import jj.script.AssociatedScriptBundle;

@Singleton
public class JJWebSocketConnection implements DataStore {
	
	private static final String CLIENT_STORAGE = "JJWebSocketConnection client storage";
	
	private final HashMap<String, Callable> functions = new HashMap<>();
	
	private final ExecutionTrace trace;
	
	private final ChannelHandlerContext ctx;
	
	private final AssociatedScriptBundle scriptBundle;
	
	private final HashMap<String, Object> data = new HashMap<>();
	
	// room for four messages initially should be good
	private final List<JJMessage> messages = new ArrayList<>(4);
	
	private final String description;
	
	// accessed from many threads
	private volatile long lastActivity = System.currentTimeMillis();

	@Inject
	JJWebSocketConnection(
		final ExecutionTrace trace,
		final ChannelHandlerContext ctx,
		final AssociatedScriptBundle scriptBundle
	) {
		this.trace = trace;
		this.ctx = ctx;
		this.scriptBundle = scriptBundle;
		description = String.format(
			"WebSocket connection to %s started at %s",
			ctx.channel().remoteAddress(),
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
	
	public Callable getFunction(String name) {
		return functions.get(name);
	}

	public void addFunction(String name, Callable function) {
		functions.put(name, function);
	}
	
	public boolean removeFunction(String name) {
		return functions.remove(name) != null;
	}
	
	public boolean removeFunction(String name, Callable function) {
		return (functions.get(name) == function) && (functions.remove(name) == function);
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
	
	public JJWebSocketConnection send(JJMessage message) {
		messages.add(message);
		return this;
	}
	
	public void end() {
		if (!messages.isEmpty()) {
			String message = serialize();
			trace.send(this, message);
			ctx.writeAndFlush(new TextWebSocketFrame(message));
		}
	}
	
	private String serialize() {
		StringBuilder output = new StringBuilder();
		if (!messages.isEmpty()) {
			output.append("[");
			for (JJMessage message : messages) {
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
		ctx.writeAndFlush(new CloseWebSocketFrame(1000, null)).addListener(ChannelFutureListener.CLOSE);
	}
}
