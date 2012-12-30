package jj.webbit;

import jj.HttpControlThread;
import jj.JJExecutors;
import jj.hostapi.HostEvent;
import jj.jqmessage.JQueryMessage;
import jj.jqmessage.JQueryMessageException;
import jj.script.ScriptBundle;
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
	
	WebSocketHandler(
		final ScriptBundleFinder scriptBundleFinder,
		final JJExecutors executors,
		final WebSocketConnections connections
	) {
		this.scriptBundleFinder = scriptBundleFinder;
		this.executors = executors;
		this.connections = connections;
	}

	@Override
	@HttpControlThread
	public void onOpen(WebSocketConnection connection) {
		
		String uri = connection.httpRequest().uri().substring(1);
		ScriptBundle actual = scriptBundleFinder.forSocketUri(uri);
		ScriptBundle latest = scriptBundleFinder.forSocketUriBaseName(uri);
		JJWebSocketConnection jjcon = new JJWebSocketConnection(connection, latest != actual);
		if (jjcon.immediateClosure()) {
			log.info("connection attempted to an old script, attempting reload");
			connection.send("{\"command\":\"reload\"}");
			connection.close();
		} else {
			log.debug("new connection to {}", latest);
			log.debug("{}", jjcon);
			jjcon.scriptBundle(latest);
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
		log.debug("received message [{}] on {}]", msg, jjcon);
		try {
			JQueryMessage message = JQueryMessage.fromString(msg);
			executors.scriptRunner().submit(jjcon, message);
			
		} catch (JQueryMessageException e) {
			log.warn("{} connected to script {} spoke gibberish to me: {}", 
				jjcon,
				jjcon.scriptBundle(),
				msg
			);
		}
		
	}

	@Override
	@HttpControlThread
	public void onMessage(WebSocketConnection connection, byte[] msg)
			throws Throwable {
		// at some point this is going to become interesting,
		// thinking about streaming bytes in for uploads...
		
	}

	@Override
	@HttpControlThread
	public void onPong(WebSocketConnection connection, byte[] msg)
			throws Throwable {
		// ping-pong will work into things soon enough, we'll need some
		// sort of slow heartbeat
	}
}