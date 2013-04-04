package jj.webbit;

import org.picocontainer.MutablePicoContainer;

public class WebbitInitializer {
	public static MutablePicoContainer initialize(final MutablePicoContainer container, final boolean isTest) {
		if (isTest) {
			// we want a new instance every time
			container.addComponent(WebbitTestRunner.class);
		} else {
			container.addComponent(WebbitBootstrapper.class);
		}
		
		return container
				.addComponent(JJEngineHttpHandler.class)	
				.addComponent(WebSocketHandler.class)
				.addComponent(WebSocketConnections.class)
				.addComponent(JJAccessLoggingHttpHandler.class)
				.addComponent(NotFoundHttpHandler.class)
				.addComponent(EventMessageProcessor.class)
				.addComponent(ResultMessageProcessor.class)
				.addComponent(ElementMessageProcessor.class);
	}
}
