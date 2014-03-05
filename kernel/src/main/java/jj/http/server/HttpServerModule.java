package jj.http.server;

import jj.JJModule;
import jj.http.server.servable.ServableModule;

public class HttpServerModule extends JJModule {
	
	@Override
	protected void configure() {

		addStartupListenerBinding().to(HttpServer.class);
		
		addConverterBinding().to(FromObjectArrayToBinding.class);
		
		addStartupListenerBinding().to(WebSocketConnectionTracker.class);
		
		install(new ServableModule());
	}
}
