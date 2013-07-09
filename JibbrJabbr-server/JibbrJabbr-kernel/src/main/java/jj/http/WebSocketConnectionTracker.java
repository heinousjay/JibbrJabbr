package jj.http;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJServerListener;
import jj.execution.JJNioEventLoopGroup;
import jj.script.AssociatedScriptBundle;

/**
 * 
 * @author jason
 *
 */
@Singleton
public class WebSocketConnectionTracker implements JJServerListener {
	
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
	ConcurrentHashMap<AssociatedScriptBundle, ConcurrentHashMap<JJWebSocketConnection, Boolean>> 
	perScript =
		new ConcurrentHashMap<>();
		
	private final JJNioEventLoopGroup eventLoopGroup;
	
	@Inject
	public WebSocketConnectionTracker(final JJNioEventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}
	
	public void start() {
		eventLoopGroup.scheduleAtFixedRate(new ActivityChecker(), 5, 5, SECONDS);
	}
	
	public void stop() {
		// nothing to do, really.  it's going to shut down anyway
	}
	
	void addConnection(JJWebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.associatedScriptBundle());
		
		if (connectionSet == null) {
			perScript.putIfAbsent(
				connection.associatedScriptBundle(),
				new ConcurrentHashMap<JJWebSocketConnection, Boolean>(16, 0.75F, 2)
			);
			connectionSet = perScript.get(connection.associatedScriptBundle());
		}
		connectionSet.putIfAbsent(connection, Boolean.TRUE);
	}
	
	void removeConnection(JJWebSocketConnection connection) {
		
		allConnections.remove(connection);
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connectionSet = 
			perScript.get(connection.associatedScriptBundle());
		
		assert (connectionSet != null) : "couldn't find a connection set to remove a connection.  impossible!";
		
		connectionSet.remove(connection);
	}
	
	public Set<JJWebSocketConnection> forScript(AssociatedScriptBundle scriptBundle) {
		
		ConcurrentHashMap<JJWebSocketConnection, Boolean> connections = perScript.get(scriptBundle);		
		
		return (connections != null) ?
			Collections.unmodifiableSet(connections.keySet()) :
			Collections.<JJWebSocketConnection>emptySet();
	}
}
