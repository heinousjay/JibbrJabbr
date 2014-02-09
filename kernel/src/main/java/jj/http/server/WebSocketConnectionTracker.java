package jj.http.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJServerStartupListener;
import jj.execution.JJNioEventLoopGroup;

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
			
			for (WebSocketConnection connection : allConnections.keySet()) {
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

	private final ConcurrentMap<WebSocketConnection, Boolean> allConnections =
		PlatformDependent.newConcurrentHashMap(16, 0.75F, 2);
		
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
	
	void addConnection(WebSocketConnection connection) {
		
		allConnections.putIfAbsent(connection, Boolean.TRUE);
	}
	
	void removeConnection(WebSocketConnection connection) {
		
		allConnections.remove(connection);
	}
}
