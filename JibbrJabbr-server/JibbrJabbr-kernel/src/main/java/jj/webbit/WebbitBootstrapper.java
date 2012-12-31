package jj.webbit;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import jj.Configuration;
import jj.JJShutdown;
import jj.JJStartup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;
import org.webbitserver.handler.logging.LoggingHandler;

class WebbitBootstrapper implements JJStartup, JJShutdown {
	
	private final Logger log = LoggerFactory.getLogger(WebbitBootstrapper.class);
	
	private final WebServer webServer;
	
	private int port(final URI uri) {
		int port = uri.getPort();
		if (port == -1) {
			port = uri.getScheme().endsWith("s") ? 443 : 80;
		}
		return port;
	}
	
	private static final ThreadFactory controlThreadFactory = new ThreadFactory() {
		
		@Override
		public Thread newThread(final Runnable r) {
			return new Thread(r, "Webbit HTTP control thread");
		}
	};

	private static final AtomicInteger sfhIOThreadFactorySeq = new AtomicInteger();
	private static final ThreadFactory sfhIOThreadFactory = new ThreadFactory() {
		
		
		@Override
		public Thread newThread(final Runnable r) {
			return new Thread(r, "Webbit StaticFileHandler I/O thread " + sfhIOThreadFactorySeq.incrementAndGet());
		}
	};

	WebbitBootstrapper(
		final Configuration configuration,
		final LoggingHandler loggingHandler,
		final JJEngineHttpHandler htmlEngineHttpHandler,
		final NotFoundHttpHandler notFoundHandler
	) throws InterruptedException, ExecutionException {

		URI uri = configuration.baseUri();
		
		webServer = 
				WebServers.createWebServer(
					Executors.newSingleThreadExecutor(controlThreadFactory), 
					new InetSocketAddress(port(uri)), 
					uri
				)
				.add(loggingHandler)
				.add(htmlEngineHttpHandler)
				// let the static file handler get stuff i'm not getting yet
				.add(new StaticFileHandler(configuration.basePath().toFile(), Executors.newFixedThreadPool(4, sfhIOThreadFactory)))
				// here would go an error handler
				.add(notFoundHandler)
				.uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						log.error("caught an exception at the bootstrap", e);
					}
				});
	}
	
	public void start() throws Exception {
		log.info("web server started at {}", webServer.start().get().getUri());
	}
	
	public void stop() {
		log.info("stopping the web server");
		try {
			webServer.stop();
		} catch (Exception eaten) {}
	}
}
