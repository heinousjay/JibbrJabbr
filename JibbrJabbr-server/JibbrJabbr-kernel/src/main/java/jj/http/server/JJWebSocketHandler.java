package jj.http.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.HostEvent;
import jj.execution.ExecutionTrace;
import jj.execution.JJExecutors;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * bridges websocket connections into the correct script execution
 * @author jason
 *
 */
@Singleton
public class JJWebSocketHandler {
	
	private Logger log = LoggerFactory.getLogger(JJWebSocketHandler.class);
	
	private final JJExecutors executors;
	
	private final ExecutionTrace trace;
	
	private final Map<JJMessage.Type, WebSocketMessageProcessor> messageProcessors;
	
	@Inject
	JJWebSocketHandler(
		final JJExecutors executors,
		final ExecutionTrace trace,
		final Set<WebSocketMessageProcessor> messageProcessors
	) {
		this.executors = executors;
		this.trace = trace;
		this.messageProcessors = makeMessageProcessors(messageProcessors);
	}
	
	private 
	Map<JJMessage.Type, WebSocketMessageProcessor> 
	makeMessageProcessors(final Set<WebSocketMessageProcessor> messageProcessors) {
		HashMap<JJMessage.Type, WebSocketMessageProcessor> result = new HashMap<>();
		for (WebSocketMessageProcessor messageProcessor : messageProcessors) {
			result.put(messageProcessor.type(), messageProcessor);
		}
		return Collections.unmodifiableMap(result);
	}

	public void opened(JJWebSocketConnection connection) {
		trace.start(connection);
		executors.scriptRunner().submit(connection, HostEvent.clientConnected, connection);
	}

	public void closed(JJWebSocketConnection connection) {
		trace.end(connection);
		executors.scriptRunner().submit(connection, HostEvent.clientDisconnected, connection);
	}

	public void messageReceived(JJWebSocketConnection connection, String msg) {
		connection.markActivity();
		trace.message(connection, msg);
		boolean success = false;
		
		try {
			JJMessage message = JJMessage.fromString(msg);
			
			if (messageProcessors.containsKey(message.type())) {
				messageProcessors.get(message.type()).handle(connection, message);
				success = true;
			}
		} catch (JJMessageException e) {}
		
		if (!success) {
			log.warn("{} spoke gibberish to me: {}", 
				connection,
				msg
			);
		}
	}

	public void messageReceived(JJWebSocketConnection connection, byte[] msg) {
		// at some point this is going to become interesting,
		// thinking about streaming bytes in for uploads...
		log.info("receiving bytes, length is {}", msg.length);
	}

	public void ponged(JJWebSocketConnection connection, byte[] msg) {
		
	}
}
