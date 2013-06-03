package jj.webbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jj.DateFormatHelper;
import jj.ExecutionTrace;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.wrapper.WebSocketConnectionWrapper;

public class JJWebSocketConnection extends WebSocketConnectionWrapper {
	
	private static final String ASSOCIATED_SCRIPT_BUNDLE = "associated script bundle";
	
	private static final String IMMEDIATE_CLOSURE = "immediate closure";
	
	private static final String MESSAGES = "messages";
	
	private static final String CLIENT_STORAGE = "client storage";
	
	private static final String LAST_ACTIVITY = "last activity";
	
	private final Logger log = LoggerFactory.getLogger(JJWebSocketConnection.class);
	
	private final ExecutionTrace trace;

	JJWebSocketConnection(final WebSocketConnection connection, final boolean immediateClosure, final ExecutionTrace trace) {
		super(connection);
		this.trace = trace;
		if (immediateClosure) {
			data(IMMEDIATE_CLOSURE, Boolean.TRUE);
		} else {
			markActivity();
		}
	}
	
	void markActivity() {
		data().put(LAST_ACTIVITY, System.currentTimeMillis());
	}
	
	long lastActivity() {
		return (long)data().get(LAST_ACTIVITY);
	}
	
	boolean immediateClosure() {
		return data().get(IMMEDIATE_CLOSURE) == Boolean.TRUE;
	}
	
	void scriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		data().put(ASSOCIATED_SCRIPT_BUNDLE, associatedScriptBundle);
	}
	
	public String baseName() {
		return associatedScriptBundle().baseName();
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return (AssociatedScriptBundle)data().get(ASSOCIATED_SCRIPT_BUNDLE);
	}
	
	public Map<String, Object> clientStorage() {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>)data().get(CLIENT_STORAGE);
		if (map == null) {
			map = new HashMap<>();
			data().put(CLIENT_STORAGE, map);
		}
		return map;
	}
	
	public JJWebSocketConnection send(JQueryMessage message) {
		messages().add(message);
		return this;
	}
	
	@Override
	public JJWebSocketConnection send(String message) {
		markActivity();
		log.trace("sending {} on {}", message, this);
		super.send(message);
		return this;
	}
	
	public void end() {
		if (!messages().isEmpty()) {
			String message = serialize();
			trace.send(this, message);
			send(message);
		}
	}
	
	private String serialize() {
		StringBuilder output = new StringBuilder();
		if (!messages().isEmpty()) {
			output.append("[");
			for (JQueryMessage message : messages()) {
				output.append(message).append(',');
			}
			output.setCharAt(output.length() - 1, ']');
			messages().clear();
		}
		return output.toString();
	}
	
	private List<JQueryMessage> messages() {
		@SuppressWarnings("unchecked")
		List<JQueryMessage> messages = (List<JQueryMessage>)data().get(MESSAGES);
		if (messages == null) {
			messages = new ArrayList<>(4);
			data().put(MESSAGES, messages);
		}
		return messages;
	}
	
	@Override
	public int hashCode() {
		return originalControl().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		// we only want object equality on our connections
		return obj instanceof JJWebSocketConnection &&
			((JJWebSocketConnection)obj).originalControl() == originalControl();
	}
	
	@Override
	public String toString() {
		return "connection[" +
			httpRequest().uri() +
			"] started at " +
			DateFormatHelper.basicFormat(httpRequest().timestamp());
	}
}
