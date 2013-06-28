package jj.http;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.HttpControlThread;
import jj.JJExecutors;
import jj.JJRunnable;
import jj.JJServerListener;
import jj.script.AssociatedScriptBundle;

/**
 * 
 * @author jason
 *
 */
@Singleton
public class WebSocketConnections implements JJServerListener {
	
	private final Logger log = LoggerFactory.getLogger(WebSocketConnections.class);
	
	private final class ActivityChecker extends JJRunnable {
		
		private ActivityChecker() {
			 super("WebSocket connection activity checker");
		}
		
		@Override
		protected boolean ignoreInExecutionTrace() {
			return true;
		}

		@Override
		public void run() throws Exception {
			
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
	
	private final 
	ConcurrentHashMap<AssociatedScriptBundle, ConcurrentHashMap<JJWebSocketConnection, Boolean>> 
	perScript =
		new ConcurrentHashMap<>();
		
	private final JJExecutors executors;
	
	@Inject
	public WebSocketConnections(final JJExecutors executors) {
		this.executors = executors;
	}
	
	public void start() {
		executors.httpControlExecutor().scheduleAtFixedRate(executors.prepareTask(new ActivityChecker()), 5, 5, SECONDS);
	}
	
	public void stop() {
		
	}
		
		
	@HttpControlThread
	void addConnection(JJWebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.associatedScriptBundle());
		
		if (connectionSet == null) {
			connectionSet = new ConcurrentHashMap<>(16, 0.75F, 2);
			perScript.put(connection.associatedScriptBundle(), connectionSet);
		}
		connectionSet.putIfAbsent(connection, Boolean.TRUE);
	}
	
	@HttpControlThread
	void removeConnection(JJWebSocketConnection connection) {
		allConnections.remove(connection);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.associatedScriptBundle());
		if (connectionSet != null) {
			connectionSet.remove(connection);
			if (connectionSet.isEmpty()) {
				perScript.remove(connection.associatedScriptBundle());
			}
		}
	}
	
	public Set<JJWebSocketConnection> forScript(AssociatedScriptBundle scriptBundle) {
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connections = perScript.get(scriptBundle);		
		return (connections != null) ? connections.keySet() : Collections.<JJWebSocketConnection>emptySet();
	}
}
