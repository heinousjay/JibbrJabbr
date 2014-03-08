package jj.http.server;

import static java.util.concurrent.TimeUnit.SECONDS;
import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

/**
 * 
 * @author jason
 *
 */
@Singleton
public class WebSocketConnectionTracker implements JJServerStartupListener {
	
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

	private final ConcurrentMap<WebSocketConnection, Boolean> allConnections =
		PlatformDependent.newConcurrentHashMap(16, 0.75F, 2);
		
	private final TaskRunner taskRunner;
	
	@Inject
	public WebSocketConnectionTracker(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}
	
	@Override
	public void start() {
		taskRunner.execute(new ActivityChecker());
	}
	
	@Override
	public Priority startPriority() {
		return Priority.Lowest;
	}
	
	void addConnection(WebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
	}
	
	void removeConnection(WebSocketConnection connection) {
		
		allConnections.remove(connection);
	}
}
