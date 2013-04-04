package jj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JJServerLifecycle {

	private final Logger log = LoggerFactory.getLogger(JJServerLifecycle.class);
	
	private final JJStartup[] startup;
	private final JJShutdown[] shutdown;
	
	JJServerLifecycle(
		final JJStartup[] startup,
		final JJShutdown[] shutdown
	) {
		this.startup = startup;
		this.shutdown = shutdown;
	}
	
	public void start() throws Exception {
		log.info("starting the server");
		for (JJStartup start: startup) {
			start.start();
		}
	}
	
	public void stop() {
		log.info("stopping the server");
		for (JJShutdown stop: shutdown) {
			try {
				stop.stop();
			} catch (Throwable t) {
				log.error("{} threw on stop, ignoring", stop);
				log.error("", t);
			}
		}
	}
}
