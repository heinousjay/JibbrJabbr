package jj.http.server.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.http.server.HttpServerStarted;

/**
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
public class WebSocketConnectionTracker {
	
	private final class ActivityChecker extends ServerTask {

		/**
		 * @param name
		 */
		public ActivityChecker() {
			super("WebSocket connection activity checker");
		}

		@Override
		public void run() {
			for (WebSocketConnection connection : allConnections.keySet()) {
				if (System.currentTimeMillis() - connection.lastActivity() > 35000) {
					connection.close();
				}
			}
			
			if (!Thread.currentThread().isInterrupted()) {
				repeat();
			}
		}
		
		@Override
		protected long delay() {
			return TimeUnit.MILLISECONDS.convert(5, SECONDS);
		}
	}

	private final ConcurrentMap<WebSocketConnection, Boolean> allConnections = new ConcurrentHashMap<>(16, 0.75F, 2);
		
	private final TaskRunner taskRunner;
	
	@Inject
	public WebSocketConnectionTracker(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}
	
	@Listener
	void start(HttpServerStarted event) {
		taskRunner.execute(new ActivityChecker());
	}
	
	void addConnection(WebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
	}
	
	void removeConnection(WebSocketConnection connection) {
		
		allConnections.remove(connection);
	}
}
