package jj.http.server;

import jj.JJModule;
import jj.http.server.methods.HttpMethodHandlerModule;
import jj.http.server.resource.StaticResource;
import jj.http.server.resource.StaticResourceCreator;
import jj.http.server.servable.ServableModule;
import jj.http.server.websocket.WebSocketConnectionTracker;

public class HttpServerModule extends JJModule {
	
	@Override
	protected void configure() {
		
		bindAPIModulePath("/jj/http/server/api");
		bindAPIModulePath("/jj/http/server/websocket/api");
		
		bindConfiguration(HttpServerSocketConfiguration.class);

		bindStartupListener(HttpServer.class);
		
		bindStartupListener(WebSocketConnectionTracker.class);
		
		bindLoggedEventsAnnotatedWith(AccessLogger.class).toLogger(AccessLogger.NAME);
		
		bindExecutor(HttpServerNioEventLoopGroup.class);
		
		bindCreationOf(StaticResource.class).to(StaticResourceCreator.class);
		
		install(new ServableModule());
		install(new HttpMethodHandlerModule());
	}
}
