package jj.http.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJServerStartupListener;
import jj.execution.JJNioEventLoopGroup;
import jj.script.DocumentScriptExecutionEnvironment;

/**
 * 
 * @author jason
 *
 */
@Singleton
public class WebSocketConnectionTracker implements JJServerStartupListener {
	
	private final Logger log = LoggerFactory.getLogger(WebSocketConnectionTracker.class);
	
	private final class ActivityChecker implements Runnable {

		@Override
		public void run() {
			
			for (JJWebSocketConnection connection : allConnections.keySet()) {
				if (System.currentTimeMillis() - connection.lastActivity() > 35000) {
					log.debug("terminating an idle connection {}", connection);
					connection.close();
				}
			}
		}
		
		@Override
		public String toString() {
			return ActivityChecker.class.getSimpleName();
		}
		
	}

	// using ConcurrentHashMap because while there will only ever be
	// one thread manipulating these structures, many threads could 
	// end up reading them
	private final ConcurrentHashMap<JJWebSocketConnection, Boolean> allConnections =
		new ConcurrentHashMap<>(16, 0.75F, 2);
	
	private final 
	ConcurrentHashMap<DocumentScriptExecutionEnvironment, ConcurrentHashMap<JJWebSocketConnection, Boolean>> 
	perScript =
		new ConcurrentHashMap<>();
		
	private final JJNioEventLoopGroup eventLoopGroup;
	
	@Inject
	public WebSocketConnectionTracker(final JJNioEventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}
	
	@Override
	public void start() {
		eventLoopGroup.scheduleAtFixedRate(new ActivityChecker(), 5, 5, SECONDS);
	}
	
	@Override
	public Priority startPriority() {
		return Priority.Lowest;
	}
	
	void addConnection(JJWebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.associatedScriptExecutionEnvironment());
		
		if (connectionSet == null) {
			perScript.putIfAbsent(
				connection.associatedScriptExecutionEnvironment(),
				new ConcurrentHashMap<JJWebSocketConnection, Boolean>(16, 0.75F, 2)
			);
			connectionSet = perScript.get(connection.associatedScriptExecutionEnvironment());
		}
		connectionSet.putIfAbsent(connection, Boolean.TRUE);
	}
	
	void removeConnection(JJWebSocketConnection connection) {
		
		allConnections.remove(connection);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.associatedScriptExecutionEnvironment());
		
		assert (connectionSet != null) : "couldn't find a connection set to remove a connection.  impossible!";
		
		connectionSet.remove(connection);
	}
	
	public Set<JJWebSocketConnection> forScript(DocumentScriptExecutionEnvironment scriptExecutionEnvironment) {
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connections = perScript.get(scriptExecutionEnvironment);		
		
		return (connections != null) ?
			Collections.unmodifiableSet(connections.keySet()) :
			Collections.<JJWebSocketConnection>emptySet();
	}
}
