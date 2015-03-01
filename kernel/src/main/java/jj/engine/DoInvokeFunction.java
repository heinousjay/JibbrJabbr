package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

@Singleton
public class DoInvokeFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DO_INVOKE = "//doInvoke";
	
	private final CurrentWebSocketConnection connection;
	private final CurrentScriptEnvironment env;
	
	@Inject
	public DoInvokeFunction(final CurrentWebSocketConnection connection, final CurrentScriptEnvironment env) {
		this.connection = connection;
		this.env = env;
	}
	
	@Override
	public String name() {
		return PROP_DO_INVOKE;
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
		assert connection.current() != null : "cannot invoke remote functions without a connected client in context";
		JJMessage message = JJMessage.makeInvoke(String.valueOf(args[0]), String.valueOf(args[1]));
		connection.current().send(message);
		System.out.println("what is up hot stuff " + message);
		throw env.preparedContinuation(message);
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
