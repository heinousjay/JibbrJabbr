package jj.http;

import jj.JJServerListener;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class HttpModule extends AbstractModule {
	
	private final boolean isTest;
	
	public HttpModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		Multibinder<JJServerListener> serverListeners = Multibinder.newSetBinder(binder(), JJServerListener.class);
		
		if (!isTest) {
			serverListeners.addBinding().to(HttpServer.class);
		}
		
		serverListeners.addBinding().to(WebSocketConnectionTracker.class);
		
		Multibinder<WebSocketMessageProcessor> messageProcessors = Multibinder.newSetBinder(binder(), WebSocketMessageProcessor.class);
		
		messageProcessors.addBinding().to(EventMessageProcessor.class);
		messageProcessors.addBinding().to(ResultMessageProcessor.class);
		messageProcessors.addBinding().to(ElementMessageProcessor.class);
	}
}
