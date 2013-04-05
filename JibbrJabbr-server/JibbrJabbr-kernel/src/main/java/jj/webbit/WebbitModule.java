package jj.webbit;

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
		
		if (isTest) {
			bind(WebbitTestRunner.class);
		} else {
			serverListeners.addBinding().to(WebbitBootstrapper.class);
		}
		
		serverListeners.addBinding().to(WebSocketConnections.class);
		
		bind(JJEngineHttpHandler.class);
		bind(org.webbitserver.WebSocketHandler.class).to(WebSocketHandler.class);
		bind(JJAccessLoggingHttpHandler.class);
		bind(NotFoundHttpHandler.class);
		
		Multibinder<WebSocketMessageProcessor> messageProcessors = Multibinder.newSetBinder(binder(), WebSocketMessageProcessor.class);
		
		messageProcessors.addBinding().to(EventMessageProcessor.class);
		messageProcessors.addBinding().to(ResultMessageProcessor.class);
		messageProcessors.addBinding().to(ElementMessageProcessor.class);
	}
}
