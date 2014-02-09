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
 * 
 * pretty much going away, it's way too thin, reorg this stuff as 
 * creating a task using a factory
 * 
 * @author jason
 *
 */
@Singleton
class WebSocketHandler {
	
	private Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
	
	private final ScriptRunner scriptRunner;
	
	
	
	@Inject
	WebSocketHandler(
		final ScriptRunner scriptRunner
	) {
		this.scriptRunner = scriptRunner;
	}

	public void opened(WebSocketConnection connection) {
		scriptRunner.submit(connection, HostEvent.clientConnected.toString(), connection);
	}

	public void closed(WebSocketConnection connection) {
		scriptRunner.submit(connection, HostEvent.clientDisconnected.toString(), connection);
	}
	
	public void errored(WebSocketConnection connection, Throwable cause) {
		
	}

	public void messageReceived(WebSocketConnection connection, ByteBuf byteBuf) {
		// at some point this is going to become interesting
		// the notion of receiving a bytebuf is not going to happen. there will be
		// some sort of streamable thing
		log.info("receiving bytes, length is {}", byteBuf.readableBytes());
	}

	public void ponged(WebSocketConnection connection, ByteBuf byteBuf) {
		
	}
}
