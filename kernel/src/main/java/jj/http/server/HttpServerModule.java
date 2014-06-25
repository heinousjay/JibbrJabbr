package jj.http.server;

import jj.JJModule;
import jj.http.server.servable.ServableModule;
import jj.http.server.uri.URIModule;

public class HttpServerModule extends JJModule {
	
	@Override
	protected void configure() {
		
		addAPIModulePath("/jj/http/server/api");
		
		bindConfiguration().to(HttpServerSocketConfiguration.class);

		addStartupListenerBinding().to(HttpServer.class);
		
		addStartupListenerBinding().to(WebSocketConnectionTracker.class);
		
		bindLoggedEvents().annotatedWith(AccessLogger.class).toLogger(AccessLogger.NAME);
		
		bindExecutor(JJNioEventLoopGroup.class);
		
		
		install(new ServableModule());
		install(new URIModule());
	}
}
