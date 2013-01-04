package jj.webbit;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.HttpControlThread;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.script.ScriptBundle;

/**
 * 
 * @author jason
 *
 */
public class WebSocketConnections {
	
	private final Logger log = LoggerFactory.getLogger(WebSocketConnections.class);
	
	private final class ActivityChecker extends JJRunnable {

		/**
		 * @param taskName
		 */
		public ActivityChecker() {
			super("WebSocket connection activity checker");
		}

		/* (non-Javadoc)
		 * @see jj.JJRunnable#innerRun()
		 */
		@Override
		protected void innerRun() throws Exception {
			
			for (JJWebSocketConnection connection : allConnections.keySet()) {
				if (System.currentTimeMillis() - connection.lastActivity() > 35000) {
					log.debug("terminating an idle connection {}", connection);
					connection.close();
				}
			}
		}
		
	}

	// using ConcurrentHashMap because while there will only ever be
	// one thread manipulating these structures, many threads could 
	// end up reading them
	private final ConcurrentHashMap<JJWebSocketConnection, Boolean> allConnections =
		new ConcurrentHashMap<>(16, 0.75F, 2);
	
	private final ConcurrentHashMap<ScriptBundle, ConcurrentHashMap<JJWebSocketConnection, Boolean>> perScript =
		new ConcurrentHashMap<>();
		
	public WebSocketConnections(final JJExecutors executors) {
		executors.httpControlExecutor().scheduleAtFixedRate(new ActivityChecker(), 5, 5, SECONDS);
	}
		
		
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
