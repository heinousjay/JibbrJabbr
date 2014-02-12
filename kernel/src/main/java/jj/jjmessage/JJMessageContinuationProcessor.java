package jj.jjmessage;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.CurrentWebSocketConnection;
import jj.script.ContinuationProcessor;
import jj.script.ContinuationState;

/**
 * Kinda simple as these things go
 * should live in the same package as the messages and get added using a facility for such things
 * @author jason
 *
 */
@Singleton
class JJMessageContinuationProcessor implements ContinuationProcessor {
	
	private final CurrentWebSocketConnection connection;
	
	@Inject
	JJMessageContinuationProcessor(final CurrentWebSocketConnection connection) {
		this.connection = connection;
	}

	@Override
	public void process(ContinuationState continuationState) {
		// not a lot to do here, these can only happen in the context
		// of a connected WebSocket client, we we just send the message
		// along to the client and let any results get handled by
		// the connection listener
		connection.current().send(continuationState.continuationAs(JJMessage.class));
	}

}
