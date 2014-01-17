package jj.http.server;

import io.netty.buffer.ByteBuf;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.engine.HostEvent;
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
	
	
	
	@Inject
	JJWebSocketHandler(
		final ScriptRunner scriptRunner
	) {
		this.scriptRunner = scriptRunner;
	}

	public void opened(JJWebSocketConnection connection) {
		scriptRunner.submit(connection, HostEvent.clientConnected.toString(), connection);
	}

	public void closed(JJWebSocketConnection connection) {
		scriptRunner.submit(connection, HostEvent.clientDisconnected.toString(), connection);
	}
	
	public void errored(JJWebSocketConnection connection, Throwable cause) {
		
	}

	public void messageReceived(JJWebSocketConnection connection, ByteBuf byteBuf) {
		// at some point this is going to become interesting
		// the notion of receiving a bytebuf is not going to happen. there will be
		// some sort of streamable thing
		log.info("receiving bytes, length is {}", byteBuf.readableBytes());
	}

	public void ponged(JJWebSocketConnection connection, ByteBuf byteBuf) {
		
	}
}
