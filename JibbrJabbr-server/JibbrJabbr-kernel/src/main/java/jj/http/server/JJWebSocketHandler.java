package jj.http.server;

import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.HostEvent;
import jj.jjmessage.JJMessage;
import jj.jjmessage.JJMessageException;
import jj.script.ScriptRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * bridges websocket connections into the correct script execution
 * @author jason
 *
 */
@Singleton
class JJWebSocketHandler {
	
	private Logger log = LoggerFactory.getLogger(JJWebSocketHandler.class);
	
	private final ScriptRunner scriptRunner;
	
	private final Map<JJMessage.Type, WebSocketMessageProcessor> messageProcessors;
	
	@Inject
	JJWebSocketHandler(
		final ScriptRunner scriptRunner,
		final Set<WebSocketMessageProcessor> messageProcessors
	) {
		this.scriptRunner = scriptRunner;
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
		scriptRunner.submit(connection, HostEvent.clientConnected, connection);
	}

	public void closed(JJWebSocketConnection connection) {
		scriptRunner.submit(connection, HostEvent.clientDisconnected, connection);
	}

	public void messageReceived(JJWebSocketConnection connection, String msg) {
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

	public void messageReceived(JJWebSocketConnection connection, ByteBuf byteBuf) {
		// at some point this is going to become interesting,
		// thinking about streaming bytes in for uploads...
		log.info("receiving bytes, length is {}", byteBuf.readableBytes());
	}

	public void ponged(JJWebSocketConnection connection, ByteBuf byteBuf) {
		
	}
}
