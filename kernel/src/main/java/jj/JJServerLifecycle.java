package jj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JJServerLifecycle {

	private final Logger log = LoggerFactory.getLogger(JJServerLifecycle.class);
	
	private final Set<JJServerStartupListener> startupListeners;
	private final Set<JJServerShutdownListener> shutdownListeners;
	
	@Inject
	JJServerLifecycle(
		final Set<JJServerStartupListener> startupListeners,
		final Set<JJServerShutdownListener> shutdownListeners
	) {
		this.startupListeners = startupListeners;
		this.shutdownListeners = shutdownListeners;
	}
	
	public void start() throws Exception {
		log.info("starting the server");
		// can't do this in the constructor because Guice gets unhappy about that.
		ArrayList<JJServerStartupListener> listeners = new ArrayList<>(startupListeners);
		Collections.sort(listeners, new Comparator<JJServerStartupListener>() {

			@Override
			public int compare(JJServerStartupListener l1, JJServerStartupListener l2) {
				return l1.startPriority().compareTo(l2.startPriority());
			}
		});
		for (JJServerStartupListener listener: listeners) {
			listener.start();
		}
	}
	
	public void stop() {
		log.info("stopping the server");
		for (JJServerShutdownListener listener: shutdownListeners) {
			try {
				listener.stop();
			} catch (Throwable t) {
				log.error("{} threw on stop, ignoring", listener);
				log.error("", t);
			}
		}
	}
}
