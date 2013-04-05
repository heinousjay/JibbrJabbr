package jj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Stage;

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
	
	private final JJServerLifecycle lifecycle;
	
	public Main(final String[] args, final boolean daemonStart) throws Exception {
		if (daemonStart) throw new IllegalStateException("This won't start correctly as a daemon anymore :(");
		
		lifecycle = Guice.createInjector(Stage.PRODUCTION, new CoreModule(args, false)).getInstance(JJServerLifecycle.class);
		
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
