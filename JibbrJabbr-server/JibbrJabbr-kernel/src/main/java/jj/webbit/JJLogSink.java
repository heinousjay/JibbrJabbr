package jj.webbit;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.EventSourceConnection;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.handler.logging.LogSink;

class JJLogSink implements LogSink {
	
	private final Logger access = LoggerFactory.getLogger("access");

	@Override
	public void httpStart(HttpRequest request) {
		access.info("{} - {}", request.method(), request.uri());
		access.debug("User-Agent {}", request.header(HttpHeaders.Names.USER_AGENT));
	}

	@Override
	public void httpEnd(HttpRequest request) {

	}

	@Override
	public void webSocketConnectionOpen(WebSocketConnection connection) {

	}

	@Override
	public void webSocketConnectionClose(WebSocketConnection connection) {

	}

	@Override
	public void webSocketInboundData(WebSocketConnection connection, String data) {

	}

	@Override
	public void webSocketInboundData(WebSocketConnection connection, byte[] message) {

	}

	@Override
	public void webSocketInboundPing(WebSocketConnection connection, byte[] msg) {

	}

	@Override
	public void webSocketInboundPong(WebSocketConnection connection, byte[] msg) {

	}

	@Override
	public void webSocketOutboundData(WebSocketConnection connection, String data) {

	}

	@Override
	public void webSocketOutboundData(WebSocketConnection connection, byte[] data) {

	}

	@Override
	public void webSocketOutboundPing(WebSocketConnection connection, byte[] msg) {

	}

	@Override
	public void webSocketOutboundPong(WebSocketConnection connection, byte[] msg) {

	}

	@Override
	public void error(HttpRequest request, Throwable error) {
		
	}

	@Override
	public void custom(HttpRequest request, String action, String data) {

	}

	@Override
	public void eventSourceConnectionOpen(EventSourceConnection connection) {

	}

	@Override
	public void eventSourceConnectionClose(EventSourceConnection connection) {

	}

	@Override
	public void eventSourceOutboundData(EventSourceConnection connection, String data) {

	}

}
