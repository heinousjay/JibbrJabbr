package jj.http.server.websocket;

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

import jj.script.FunctionContext;
import jj.util.DateFormatHelper;
import jj.util.CurrentResourceAware;

@Singleton
public class WebSocketConnection implements FunctionContext, CurrentResourceAware {
	
	// start off with room for three functions
	private final HashMap<String, Callable> functions = new HashMap<>(4);
	
	private final ChannelHandlerContext ctx;
	
	private final WebSocketConnectionHost webSocketConnectionHost;
	
	private final HashMap<String, Object> clientStorage = new HashMap<>(4);
	
	private final List<WebSocketMessage> messages = new ArrayList<>(4);
	
	private final String description;
	
	// accessed from many threads
	private volatile long lastActivity = System.currentTimeMillis();

	@Inject
	WebSocketConnection(
		final ChannelHandlerContext ctx,
		final WebSocketConnectionHost webSocketConnectionHost
	) {
		this.ctx = ctx;
		this.webSocketConnectionHost = webSocketConnectionHost;
		description = String.format(
			"WebSocket connection to %s started at %s",
			ctx.channel().remoteAddress(),
			DateFormatHelper.nowInBasicFormat()
		);
	}
	
	@Override
	public Callable getFunction(String name) {
		return functions.get(name);
	}

	@Override
	public void addFunction(String name, Callable function) {
		functions.put(name, function);
	}
	
	@Override
	public boolean removeFunction(String name) {
		return functions.remove(name) != null;
	}
	
	@Override
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
		return webSocketConnectionHost().name();
	}
	
	public WebSocketConnectionHost webSocketConnectionHost() {
		return webSocketConnectionHost;
	}
	
	public Map<String, Object> clientStorage() {
		return clientStorage;
	}
	
	public WebSocketConnection send(WebSocketMessage message) {
		messages.add(message);
		return this;
	}
	
	@Override
	public void enteredCurrentScope() {
		// nothing to do
	}
	
	@Override
	public void exitedCurrentScope() {
		if (!messages.isEmpty()) {
			String message = serialize();
			ctx.writeAndFlush(new TextWebSocketFrame(message));
		}
	}
	
	private String serialize() {
		StringBuilder output = new StringBuilder();
		if (!messages.isEmpty()) {
			output.append("[");
			for (WebSocketMessage message : messages) {
				output.append(message.stringify()).append(',');
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
