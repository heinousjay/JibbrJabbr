package jj.webbit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jj.HttpControlThread;
import jj.script.ScriptBundle;

/**
 * 
 * @author jason
 *
 */
public class WebSocketConnections {

	// using ConcurrentHashMap because while there will only ever be
	// one thread manipulating these structures, many threads could 
	// end up reading them
	private final ConcurrentHashMap<JJWebSocketConnection, Boolean> allConnections =
		new ConcurrentHashMap<>(16, 0.75F, 2);
	
	// the map doesn't need to be concurrent
	// because it will only ever be accessed 
	// by the single incoming IO thread,
	// but individual connection sets will 
	// be accessed through here and from
	// a script executor
	private final HashMap<ScriptBundle, ConcurrentHashMap<JJWebSocketConnection, Boolean>> perScript =
		new HashMap<>();
		
	@HttpControlThread
	void addConnection(JJWebSocketConnection connection) {
		allConnections.putIfAbsent(connection, Boolean.TRUE);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = perScript.get(connection.scriptBundle());
		
		if (connectionSet == null) {
			connectionSet = new ConcurrentHashMap<>(16, 0.75F, 2);
			perScript.put(connection.scriptBundle(), connectionSet);
		}
		connectionSet.putIfAbsent(connection, Boolean.TRUE);
	}
	
	@HttpControlThread
	void removeConnection(JJWebSocketConnection connection) {
		allConnections.remove(connection);
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.scriptBundle());
		if (connectionSet != null) {
			connectionSet.remove(connection);
			if (connectionSet.isEmpty()) {
				perScript.remove(connection.scriptBundle());
			}
		}
	}
	
	public Set<JJWebSocketConnection> forScript(ScriptBundle scriptBundle) {
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connections = perScript.get(scriptBundle);		
		return (connections != null) ? connections.keySet() : Collections.<JJWebSocketConnection>emptySet();
	}
}
