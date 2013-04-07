package jj.webbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJServerListener;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class WebbitModule extends AbstractModule {
	
	private final boolean isTest;
	
	public WebbitModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		Multibinder<JJServerListener> serverListeners = Multibinder.newSetBinder(binder(), JJServerListener.class);
		
		if (!isTest) {
			serverListeners.addBinding().to(WebbitBootstrapper.class);
		}
		
		serverListeners.addBinding().to(WebSocketConnections.class);
		
		bind(org.webbitserver.WebSocketHandler.class).to(WebSocketHandler.class);
		
		
		
		Multibinder<WebSocketMessageProcessor> messageProcessors = Multibinder.newSetBinder(binder(), WebSocketMessageProcessor.class);
		
		messageProcessors.addBinding().to(EventMessageProcessor.class);
		messageProcessors.addBinding().to(ResultMessageProcessor.class);
		messageProcessors.addBinding().to(ElementMessageProcessor.class);
	}
}
