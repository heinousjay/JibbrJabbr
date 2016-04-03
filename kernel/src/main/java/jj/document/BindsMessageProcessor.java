package jj.document;

import com.google.inject.multibindings.MapBinder;
import jj.HasBinder;
import jj.jjmessage.JJMessage;

/**
 * Mix-in to bind a message processor to a specific message type
 *
 * Created by jasonmiller on 4/3/16.
 */
interface BindsMessageProcessor extends HasBinder {

	interface Using {
		void using(Class<? extends DocumentWebSocketMessageProcessor> processorClass);
	}

	default Using processJJMessage(JJMessage.Type type) {
		return processorClass ->
			MapBinder.newMapBinder(binder(), JJMessage.Type.class, DocumentWebSocketMessageProcessor.class).addBinding(type).to(processorClass);
	}
}
