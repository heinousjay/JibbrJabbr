package jj;

import static java.util.concurrent.TimeUnit.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

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
		
		Runtime.getRuntime().addShutdownHook(new Thread(main::stop));
	}
	
	private final String[] args;
	
	private Injector injector;
	
	private JJServerLifecycle lifecycle;
	
	private ResourceResolver systemResources;
	
	private volatile Exception initFailure;
	
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
	}
	
	public void start() throws Exception {
		init();
		
		CountDownLatch latch = new CountDownLatch(1);
		
		new Thread(() -> {
			try {
				latch.countDown();
				lifecycle.start();
				Thread.currentThread().join();
			} catch (ProvisionException pe) {
				displayMessages(pe.getErrorMessages());
				initFailure = pe;
			} catch (InterruptedException ie) {
				// do nothing and like it
			} catch (Exception e) {
				initFailure = e;
			}
		}, "Server Init").start();
		
		boolean result = latch.await(1, SECONDS);
		
		if (initFailure != null) {
			throw initFailure;
		}
		
		assert result : "timed out waiting for init";
		
	}
	
	public void stop() {
		lifecycle.stop();
	}
	
	public void dispose() {
		// if we daemon start again?
	}

}
