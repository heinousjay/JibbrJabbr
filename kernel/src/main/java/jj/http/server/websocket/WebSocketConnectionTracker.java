package jj.http.server.websocket;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.http.server.HttpServerStarted;
import jj.http.server.HttpServerStopped;

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
			// do nothing if we've been interrupted
			if (!Thread.currentThread().isInterrupted()) {
				for (WebSocketConnection connection : allConnections.keySet()) {
					if (System.currentTimeMillis() - connection.lastActivity() > 35000) {
						connection.close();
					}
				}
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
	
	private final AtomicReference<ActivityChecker> currentActivityChecker = new AtomicReference<>();
	
	@Inject
	public WebSocketConnectionTracker(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}
	
	@Listener
	void on(HttpServerStarted event) {
		if (currentActivityChecker.get() == null) {
			currentActivityChecker.set(new ActivityChecker());
			taskRunner.execute(currentActivityChecker.get());
		}
	}
	
	@Listener
	void on(HttpServerStopped event) {
		ActivityChecker current = currentActivityChecker.getAndSet(null);
		assert current != null : "server was running with no web socket activity checker!";
		current.cancelKey().cancel();
	}
	
	void addConnection(WebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
	}
	
	void removeConnection(WebSocketConnection connection) {
		
		allConnections.remove(connection);
	}
}
