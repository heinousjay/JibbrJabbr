package jj.webbit;

import org.picocontainer.MutablePicoContainer;

public class WebbitInitializer {
	public static MutablePicoContainer initialize(MutablePicoContainer container) {
		return container
				.addComponent(WebbitBootstrapper.class)
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
