package jj.webbit;

import java.net.InetSocketAddress;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.Configuration;
import jj.JJExecutors;
import jj.JJServerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

@Singleton
class WebbitBootstrapper implements JJServerListener {
	
	private final Logger log = LoggerFactory.getLogger(WebbitBootstrapper.class);
	
	private final WebServer webServer;
	
	private int port(final URI uri) {
		int port = uri.getPort();
		if (port == -1) {
			port = uri.getScheme().endsWith("s") ? 443 : 80;
		}
		return port;
	}

	@Inject
	WebbitBootstrapper(
		final Configuration configuration,
		final JJExecutors jjExecutors,
		final JJExecutiveHttpHandler executiveHandler,
		final JJEngineHttpHandler htmlEngineHttpHandler,
		final NotFoundHttpHandler notFoundHandler
	) {

		URI uri = configuration.baseUri();
		
		webServer = 
				WebServers.createWebServer(
					jjExecutors.httpControlExecutor(), 
					new InetSocketAddress(port(uri)), 
					uri
				)
				.add(executiveHandler)
				.add(htmlEngineHttpHandler)
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
