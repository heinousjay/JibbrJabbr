package jj.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jj.DataStore;
import jj.ExecutionTrace;
import jj.jqmessage.JQueryMessage;
import jj.script.AssociatedScriptBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JJWebSocketConnection implements DataStore {
	
	private static final String ASSOCIATED_SCRIPT_BUNDLE = "associated script bundle";
	
	private static final String IMMEDIATE_CLOSURE = "immediate closure";
	
	private static final String MESSAGES = "messages";
	
	private static final String CLIENT_STORAGE = "client storage";
	
	private static final String LAST_ACTIVITY = "last activity";
	
	private final Logger log = LoggerFactory.getLogger(JJWebSocketConnection.class);
	
	private final ExecutionTrace trace;
	
	private final HashMap<String, Object> data = new HashMap<>();

	JJWebSocketConnection(final boolean immediateClosure, final ExecutionTrace trace) {
		this.trace = trace;
		if (immediateClosure) {
			data.put(IMMEDIATE_CLOSURE, Boolean.TRUE);
		} else {
			markActivity();
		}
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
		data.put(LAST_ACTIVITY, System.currentTimeMillis());
	}
	
	long lastActivity() {
		return (long)data.get(LAST_ACTIVITY);
	}
	
	boolean immediateClosure() {
		return data.get(IMMEDIATE_CLOSURE) == Boolean.TRUE;
	}
	
	void scriptBundle(AssociatedScriptBundle associatedScriptBundle) {
		data.put(ASSOCIATED_SCRIPT_BUNDLE, associatedScriptBundle);
	}
	
	public String baseName() {
		return associatedScriptBundle().baseName();
	}
	
	public AssociatedScriptBundle associatedScriptBundle() {
		return (AssociatedScriptBundle)data.get(ASSOCIATED_SCRIPT_BUNDLE);
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
		messages().add(message);
		return this;
	}
	
	public JJWebSocketConnection send(String message) {
		markActivity();
		log.trace("sending {} on {}", message, this);
		
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
		List<JQueryMessage> messages = (List<JQueryMessage>)data.get(MESSAGES);
		if (messages == null) {
			messages = new ArrayList<>(4);
			data.put(MESSAGES, messages);
		}
		return messages;
	}
	
	@Override
	public String toString() {
		return "connection";
	}

	/**
	 * @return
	 */
	public String uri() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 */
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
