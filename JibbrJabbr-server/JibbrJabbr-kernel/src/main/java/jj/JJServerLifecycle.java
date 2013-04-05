package jj;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JJServerLifecycle {

	private final Logger log = LoggerFactory.getLogger(JJServerLifecycle.class);
	
	private final Set<JJServerListener> listeners;
	
	@Inject
	JJServerLifecycle(
		final Set<JJServerListener> listeners
	) {
		this.listeners = listeners;
	}
	
	public void start() throws Exception {
		log.info("starting the server");
		for (JJServerListener start: listeners) {
			start.start();
		}
	}
	
	public void stop() {
		log.info("stopping the server");
		for (JJServerListener stop: listeners) {
			try {
				stop.stop();
			} catch (Throwable t) {
				log.error("{} threw on stop, ignoring", stop);
				log.error("", t);
			}
		}
	}
}
