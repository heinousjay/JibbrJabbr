package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.CurrentWebSocketConnection;
import jj.jjmessage.JJMessage;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

@Singleton
class DoStoreFunction extends BaseFunction implements HostObject, ContributesScript {
	private static final long serialVersionUID = 1L;
	
	private static final String PROP_DO_STORE = "//doStore";
	
	private final CurrentWebSocketConnection connection;
	
	@Inject
	public DoStoreFunction(final CurrentWebSocketConnection connection) {
		this.connection = connection;
	}
	
	@Override
	public String name() {
		return PROP_DO_STORE;
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
	public String script() {
		return "function store(key, value){global['" + PROP_DO_STORE + "'](key, JSON.stringify(value));}";
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (connection.current() == null) {
			throw new IllegalStateException("cannot store remote info without a connected client in context");
		}
		connection.current().send(JJMessage.makeStore(String.valueOf(args[0]), String.valueOf(args[1])));
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
