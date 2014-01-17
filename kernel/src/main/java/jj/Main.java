package jj;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.spi.Message;

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
		
		try {
			lifecycle = Guice.createInjector(Stage.PRODUCTION, new CoreModule(args, false)).getInstance(JJServerLifecycle.class);
		} catch (CreationException ce) {
			displayMessages(ce.getErrorMessages());
			throw failStartup();
		} catch (ProvisionException pe) {
			displayMessages(pe.getErrorMessages());
			throw failStartup();
		}
		
		log.info("Welcome to {} version {} commit {}", Version.name, Version.version, Version.commitId);
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
	
	public void start() throws Exception {
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
