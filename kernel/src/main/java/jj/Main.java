package jj;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.spi.Message;

public class Main {
	
	/**
	 * debugging startup
	 */
	public static void main(String[] args) throws Exception {
		
		final Main main = new Main(args);
		
		main.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				main.stop();
			}
		});
	}
	
	private final Logger log = LoggerFactory.getLogger(Main.class);
	
	private final String[] args;
	
	private Injector injector;
	
	private JJServerLifecycle lifecycle;
	
	private ResourceResolver systemResources;
	
	public Main(final String[] args) {
		this.args = args;
	}
	
	public void systemJars(final Jars systemJars) {
		this.systemResources = systemJars;
	}
	
	private InitializationException failStartup() {
		throw new InitializationException("couldn't start!");
	}
	
	private void displayMessages(Collection<Message> messages) {
		HashSet<String> uniqueMessages = new HashSet<>();
		for (Message message : messages) {
			uniqueMessages.add(message.getCause().getMessage());
		}
		
		System.err.println("INITIALIZATION ERROR!");
		for (String uniqueMessage : uniqueMessages) {
			System.err.println(uniqueMessage);
		}
	}
	
	private void init() {
		
		if (systemResources == null) systemResources = new BootstrapClassPath();
		
		try {
			injector = Guice.createInjector(Stage.PRODUCTION, new CoreModule(args, systemResources));
			lifecycle = injector.getInstance(JJServerLifecycle.class);
		} catch (CreationException ce) {
			displayMessages(ce.getErrorMessages());
			throw failStartup();
		} catch (ProvisionException pe) {
			displayMessages(pe.getErrorMessages());
			throw failStartup();
		}
		
		Version version = injector.getInstance(Version.class);
		
		log.info("Welcome to {} version {} commit {}", version.name(), version.version(), version.commitId());
		if (!JJ.isRunning) {
			log.info("******************************************************************************");
			log.info("!!!!!!          This server is running in a nonstandard mode            !!!!!!");
			log.info("******************************************************************************");
		}
		
	}
	
	public void start() throws Exception {
		init();
		
		try {
			lifecycle.start();
		} catch (ProvisionException pe) {
			displayMessages(pe.getErrorMessages());
			throw failStartup();
		}
	}
	
	public void stop() {
		lifecycle.stop();
	}
	
	public void dispose() {
		// we can daemon start again!
	}

}
