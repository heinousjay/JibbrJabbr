package jj.webbit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jj.HttpControlThread;
import jj.JJExecutors;
import jj.hostapi.HostEvent;
import jj.jqmessage.JQueryMessage;
import jj.jqmessage.JQueryMessageException;
import jj.script.AssociatedScriptBundle;
import jj.script.ScriptBundleFinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;

/**
 * bridges websocket connections into the correct script execution
 * @author jason
 *
 */
class WebSocketHandler extends BaseWebSocketHandler {
	
	private Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
	
	private final ScriptBundleFinder scriptBundleFinder;
	
	private final JJExecutors executors;
	
	private final WebSocketConnections connections;
	
	private final Map<JQueryMessage.Type, WebSocketMessageProcessor> messageProcessors;
	
	WebSocketHandler(
		final ScriptBundleFinder scriptBundleFinder,
		final JJExecutors executors,
		final WebSocketConnections connections,
		final WebSocketMessageProcessor[] messageProcessors
	) {
		this.scriptBundleFinder = scriptBundleFinder;
		this.executors = executors;
		this.connections = connections;
		this.messageProcessors = makeMessageProcessors(messageProcessors);
	}
	
	private 
	Map<JQueryMessage.Type, WebSocketMessageProcessor> 
	makeMessageProcessors(final WebSocketMessageProcessor[] messageProcessors) {
		HashMap<JQueryMessage.Type, WebSocketMessageProcessor> result = new HashMap<>();
		for (WebSocketMessageProcessor messageProcessor : messageProcessors) {
			result.put(messageProcessor.type(), messageProcessor);
		}
		return Collections.unmodifiableMap(result);
	}

	@Override
	@HttpControlThread
	public void onOpen(WebSocketConnection connection) {
		
		String uri = connection.httpRequest().uri().substring(1);
		AssociatedScriptBundle scriptBundle = scriptBundleFinder.forSocketUri(uri);
		JJWebSocketConnection jjcon = new JJWebSocketConnection(connection, scriptBundle == null);
		if (jjcon.immediateClosure()) {
			log.info("connection attempted to an old script, attempting reload");
			// need some way of noticing we are being hammered here?
			// i mean i guess we just close em as they come in
			connection.send("jj-reload");
			connection.close();
		} else {
			log.info("new connection to {}", scriptBundle);
			log.trace("{}", jjcon);
			jjcon.scriptBundle(scriptBundle);
			connections.addConnection(jjcon);
			executors.scriptRunner().submit(jjcon, HostEvent.clientConnected, connection);
		}
	}

	@Override
	@HttpControlThread
	public void onClose(WebSocketConnection connection) {
		JJWebSocketConnection jjcon = new JJWebSocketConnection(connection, false);
		// don't do anything reload command
		if (!jjcon.immediateClosure()) {
			executors.scriptRunner().submit(jjcon, HostEvent.clientDisconnected, connection);
			connections.removeConnection(jjcon);
		}
	}

	@Override
	@HttpControlThread
	public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
		JJWebSocketConnection jjcon = new JJWebSocketConnection(connection, false);
		log.trace("received message [{}] on {}", msg, jjcon);
		if ("jj-hi".equals(msg)) {
			connection.send("jj-yo");
		} else {
			boolean success = false;
			
			try {
				JQueryMessage message = JQueryMessage.fromString(msg);
				
				if (messageProcessors.containsKey(message.type())) {
					messageProcessors.get(message.type()).handle(jjcon, message);
					success = true;
				}
			} catch (JQueryMessageException e) {}
			
			if (!success) {
				log.warn("{} spoke gibberish to me: {}", 
					jjcon,
					msg
				);
			}
		}
	}

	@Override
	@HttpControlThread
	public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
		// at some point this is going to become interesting,
		// thinking about streaming bytes in for uploads...
		
	}

	@Override
	@HttpControlThread
	public void onPong(WebSocketConnection connection, byte[] msg) throws Throwable {
		
	}
}
