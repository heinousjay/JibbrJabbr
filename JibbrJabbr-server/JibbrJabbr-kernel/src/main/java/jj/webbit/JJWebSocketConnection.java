package jj.webbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jj.DateFormatHelper;
import jj.jqmessage.JQueryMessage;
import jj.script.ScriptBundle;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.wrapper.WebSocketConnectionWrapper;

public class JJWebSocketConnection extends WebSocketConnectionWrapper {
	
	private static final String SCRIPT_BUNDLE = "script bundle";
	
	private static final String IMMEDIATE_CLOSURE = "immediate closure";
	
	private static final String MESSAGES = "messages";
	
	private static final String CLIENT_STORAGE = "client storage";

	JJWebSocketConnection(final WebSocketConnection connection, final boolean immediateClosure) {
		super(connection);
		if (immediateClosure) {
			data().put(IMMEDIATE_CLOSURE, Boolean.TRUE);
		}
	}
	
	boolean immediateClosure() {
		return data().get(IMMEDIATE_CLOSURE) == Boolean.TRUE;
	}
	
	void scriptBundle(ScriptBundle scriptBundle) {
		data().put(SCRIPT_BUNDLE, scriptBundle);
	}
	
	public String baseName() {
		return scriptBundle().baseName();
	}
	
	public ScriptBundle scriptBundle() {
		return (ScriptBundle)data().get(SCRIPT_BUNDLE);
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
	
	public void end() {
		if (!messages().isEmpty()) {
			send(serialize());
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
			httpRequest().remoteAddress() +
			"] started at " +
			DateFormatHelper.basicFormat(httpRequest().timestamp()) +
			" with data " +
			data();
	}
}
