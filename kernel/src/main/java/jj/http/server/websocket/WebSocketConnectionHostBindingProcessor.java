package jj.http.server.websocket;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

import jj.resource.ResourceBindingProcessor;

public class WebSocketConnectionHostBindingProcessor implements ResourceBindingProcessor<WebSocketConnectionHost> {

	private final Multibinder<Class<? extends WebSocketConnectionHost>> hostBinder;
	
	public WebSocketConnectionHostBindingProcessor(Binder binder) {
		hostBinder = Multibinder.newSetBinder(binder, new TypeLiteral<Class<? extends WebSocketConnectionHost>>() {});
	}

	@Override
	public void process(Class<? extends WebSocketConnectionHost> resourceClassBinding) {
		hostBinder.addBinding().toInstance(resourceClassBinding);
	}

}
