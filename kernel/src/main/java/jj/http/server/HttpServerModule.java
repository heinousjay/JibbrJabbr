package jj.http.server;

import jj.JJModule;
import jj.configuration.BindsConfiguration;
import jj.execution.BindsExecutor;
import jj.http.server.resource.StaticResource;
import jj.http.server.resource.StaticResourceCreator;
import jj.http.server.websocket.WebSocketConnectionTracker;
import jj.logging.BindsLogger;
import jj.resource.BindsResourceCreation;
import jj.server.BindsServerPath;

public class HttpServerModule extends JJModule
	implements BindsConfiguration,
		BindsExecutor,
		BindsLogger,
		BindsResourceCreation,
	BindsServerPath {
	
	@Override
	protected void configure() {
		
		bindAPIModulePath("/jj/http/server/api");
		bindAPIModulePath("/jj/http/server/websocket/api");
		
		bindConfiguration(HttpServerSocketConfiguration.class);

		bindStartupListener(HttpServer.class);
		
		bindStartupListener(WebSocketConnectionTracker.class);
		
		bindLoggedEventsAnnotatedWith(AccessLogger.class).toLogger(AccessLogger.NAME);
		
		bindExecutor(HttpServerNioEventLoopGroup.class);

		createResource(StaticResource.class).using(StaticResourceCreator.class);
	}
}
