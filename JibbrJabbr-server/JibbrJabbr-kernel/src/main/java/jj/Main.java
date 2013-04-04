package jj;

import org.picocontainer.PicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	/**
	 * debugging startup
	 */
	public static void main(String[] args) throws Exception {
		
		final Main main = new Main(args, false);
		
		main.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				main.stop();
			}
		});
	}
	
	private final Logger log = LoggerFactory.getLogger(Main.class);
	
	private final PicoContainer container;
	
	private final JJServerLifecycle lifecycle;
	
	public Main(final String[] args, final boolean daemonStart) throws Exception {
		if (daemonStart) throw new IllegalStateException("This won't start correctly as a daemon anymore :(");
		
		container = new Startup(args, false).container();
		lifecycle = container.getComponent(JJServerLifecycle.class);
		
		//lifecycle();
		log.info("Welcome to {} version {} built on {}", Version.name, Version.version, Version.buildDate);
	}
	
	public void start() throws Exception {
		lifecycle.start();
	}
	
	public void stop() {
		lifecycle.stop();
	}
	
	public void dispose() {
		// nothing to do until i figure out how
		// to get the daemon start on binding.  need
		// to modify webbit to support this
	}

}
