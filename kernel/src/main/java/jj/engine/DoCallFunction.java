package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.jjmessage.JJMessage;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

@Singleton
public class DoCallFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DO_CALL = "//doCall";
	
	private final CurrentWebSocketConnection connection;
	
	@Inject
	public DoCallFunction(final CurrentWebSocketConnection connection) {
		this.connection = connection;
	}
	
	@Override
	public String name() {
		return PROP_DO_CALL;
	}
	
	@Override
	public boolean constant() {
		return true;
	}
	
	@Override
	public boolean readonly() {
		return true;
	}
	
	@Override
	public boolean permanent() {
		return true;
	}
	
	@Override
	public boolean dontenum() {
		return true;
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		assert connection.current() != null : "cannot call remote functions without a connected client in context";
		
		connection.current().send(JJMessage.makeCall(String.valueOf(args[0]), String.valueOf(args[1])));
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
