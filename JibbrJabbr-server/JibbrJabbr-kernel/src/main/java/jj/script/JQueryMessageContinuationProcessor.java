package jj.script;

/**
 * Kinda simple as these things go
 * @author jason
 *
 */
class JQueryMessageContinuationProcessor implements ContinuationProcessor {
	
	private final CurrentScriptContext context;
	
	JQueryMessageContinuationProcessor(final CurrentScriptContext context) {
		this.context = context;
	}

	@Override
	public ContinuationType type() {
		return ContinuationType.JQueryMessage;
	}

	@Override
	public void process(ContinuationState continuationState) {
		// not a lot to do here, these can only happen in the context
		// of a connected WebSocket client, we we just send the message
		// along to the client and let any results get handled by
		// the connection listener
		context.connection().send(continuationState.jQueryMessage());
	}

}
