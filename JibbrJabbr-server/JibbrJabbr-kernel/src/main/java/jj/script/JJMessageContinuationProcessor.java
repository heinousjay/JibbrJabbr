package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Kinda simple as these things go
 * @author jason
 *
 */
@Singleton
class JJMessageContinuationProcessor implements ContinuationProcessor {
	
	private final CurrentScriptContext context;
	
	@Inject
	JJMessageContinuationProcessor(final CurrentScriptContext context) {
		this.context = context;
	}

	@Override
	public ContinuationType type() {
		return ContinuationType.JJMessage;
	}

	@Override
	public void process(ContinuationState continuationState) {
		// not a lot to do here, these can only happen in the context
		// of a connected WebSocket client, we we just send the message
		// along to the client and let any results get handled by
		// the connection listener
		context.connection().send(continuationState.jjMessage());
	}

}
