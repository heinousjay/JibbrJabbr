package jj;

import static org.picocontainer.Characteristics.HIDE_IMPL;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import jj.document.DocumentInitializer;
import jj.hostapi.HostApiInitializer;
import jj.resource.ResourceInitializer;
import jj.script.ScriptInitializer;
import jj.servable.ServableInitializer;
import jj.webbit.WebbitInitializer;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.AdaptingBehavior;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.lifecycle.NullLifecycleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

public class Main {
	
	// for now, the address is hardcoded?  this is bad
	
	private static final URI LOCAL_WORLD = URI.create("http://localhost:8080/");

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
	
	private final JJServerLifecycle serverLifecycle;
	
	public Main(String[] args, boolean daemonStart) throws IOException {
		if (daemonStart) throw new IllegalStateException("This won't start correctly as a daemon anymore :(");
		
		// arg[0] must be our URI,
		// arg[1] must be our path
		
		final SLF4JConfiguration slf4j = new SLF4JConfiguration();
		
		log.info("Welcome to JibbrJabbr");
		
		MutablePicoContainer container = 
			new DefaultPicoContainer(
				new Caching().wrap(new AdaptingBehavior()),
				new NullLifecycleStrategy(),
				null,
				new JibbrJabbrComponentMonitor()
			)
			// configuration - won't live long like this
			.addComponent(LOCAL_WORLD)
			.addComponent(Paths.get(args[0]).toRealPath())
			
			.addComponent(slf4j)
			
			.addComponent(JJServerLifecycle.class)
			.addComponent(IOExecutor.class)
			
			// a good place to break apart crafty circular dependencies
			.as(HIDE_IMPL).addComponent(JJExecutors.class, JJExecutorsImpl.class)

			// needs to be smarter configuration? i at least should be
			// supplying the executor
			.addComponent(new AsyncHttpClientConfig.Builder()
				.setCompressionEnabled(true)
				.setUserAgent("JibbrJabbr RestCall subsystem/Netty 3.5.11Final")
				.setIOThreadMultiplier(1)
				.setFollowRedirects(true)
				.build()
			)
			.addComponent(AsyncHttpClient.class);
		
		ServableInitializer.initialize(container);
		DocumentInitializer.initialize(container);
		ScriptInitializer.initialize(container);
		ResourceInitializer.initialize(container);
		HostApiInitializer.initialize(container);
		WebbitInitializer.initialize(container);
		
		serverLifecycle = container.getComponent(JJServerLifecycle.class);
	}
	
	public void start() throws Exception {
		serverLifecycle.start();
	}
	
	public void stop() {
		serverLifecycle.stop();
	}
	
	public void dispose() {
		// nothing to do until i figure out how
		// to get the daemon start on binding.  need
		// to modify webbit to support this
	}

}
