package jj.http.server;

import jj.JJModule;
import jj.http.server.servable.ServableModule;
import jj.http.server.servable.document.DocumentModule;

import com.google.inject.multibindings.Multibinder;

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
		
		addStartupListenerBinding().to(WebSocketConnectionTracker.class);
		
		Multibinder<WebSocketMessageProcessor> messageProcessors = Multibinder.newSetBinder(binder(), WebSocketMessageProcessor.class);
		
		messageProcessors.addBinding().to(EventMessageProcessor.class);
		messageProcessors.addBinding().to(ResultMessageProcessor.class);
		messageProcessors.addBinding().to(ElementMessageProcessor.class);
		
		install(new ServableModule());
		install(new DocumentModule());
	}
}
