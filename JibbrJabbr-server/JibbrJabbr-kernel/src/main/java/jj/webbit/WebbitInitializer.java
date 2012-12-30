package jj.webbit;

import org.picocontainer.MutablePicoContainer;
import org.webbitserver.handler.logging.LoggingHandler;

public class WebbitInitializer {
	public static MutablePicoContainer initialize(MutablePicoContainer container) {
		return container
				.addComponent(WebbitBootstrapper.class)
				.addComponent(JJEngineHttpHandler.class)	
				.addComponent(WebSocketHandler.class)
				.addComponent(WebSocketConnections.class)
				.addComponent(JJLogSink.class)
				.addComponent(LoggingHandler.class)
				.addComponent(NotFoundHttpHandler.class);
	}
}
