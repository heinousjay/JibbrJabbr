package jj.http.server;

import jj.JJModule;
import jj.http.server.servable.ServableModule;
import jj.http.server.servable.document.DocumentModule;

public class HttpServerModule extends JJModule {
	
	private final boolean isTest;
	
	public HttpServerModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		if (!isTest) {
			addStartupListenerBinding().to(HttpServer.class);
			addShutdownListenerBinding().to(HttpServer.class);
		}
		
		addConverterBinding().to(FromObjectArrayToBinding.class);
		
		addStartupListenerBinding().to(WebSocketConnectionTracker.class);
		
		install(new ServableModule());
		install(new DocumentModule());
	}
}
