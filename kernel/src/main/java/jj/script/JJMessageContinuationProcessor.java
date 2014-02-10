package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.CurrentWebSocketConnection;
import jj.jjmessage.JJMessage;

/**
 * Kinda simple as these things go
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
		connection.current().send(continuationState.continuableAs(JJMessage.class));
	}

}
