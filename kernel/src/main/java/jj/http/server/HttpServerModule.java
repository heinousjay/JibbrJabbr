package jj.http.server;

import jj.JJModule;
import jj.http.server.methods.HttpMethodHandlerModule;
import jj.http.server.servable.ServableModule;
import jj.http.server.websocket.WebSocketConnectionTracker;

public class HttpServerModule extends JJModule {
	
	@Override
	protected void configure() {
		
		addAPIModulePath("/jj/http/server/api");
		addAPIModulePath("/jj/http/server/websocket/api");
		
		bindConfiguration().to(HttpServerSocketConfiguration.class);

		addStartupListenerBinding().to(HttpServer.class);
		
		addStartupListenerBinding().to(WebSocketConnectionTracker.class);
		
		bindLoggedEvents().annotatedWith(AccessLogger.class).toLogger(AccessLogger.NAME);
		
		bindExecutor(HttpServerNioEventLoopGroup.class);
		
		install(new ServableModule());
		install(new HttpMethodHandlerModule());
	}
}
