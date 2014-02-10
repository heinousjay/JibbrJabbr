package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.CurrentScriptEnvironment;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * the provider of the broadcast function
 * @author jason
 *
 */
@Singleton
class BroadcastFunction extends BaseFunction implements HostObject, ContributesScript {
	private static final long serialVersionUID = 1L;
	
	private static final String GET_WEBSOCKET_CONNECTION_HOST = "//getWebSocketConnectionHost";
	
	private final CurrentScriptEnvironment env;
	
	@Inject
	BroadcastFunction(final CurrentScriptEnvironment env) {
		this.env = env;
	}
	
	@Override
	public String name() {
		return GET_WEBSOCKET_CONNECTION_HOST;
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
		return 
			"function broadcast(func) {" +
				"if (typeof func !== 'function') throw 'broadcast requires a function';" +
				"var host = global['" + GET_WEBSOCKET_CONNECTION_HOST + "']();" +
				"host.startBroadcasting();" +
				"try {" +
					"while(host.nextConnection()) func();" +
				"} finally {" +
					"host.endBroadcasting();" +
				"}" +
			"}";
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		return env.currentWebSocketConnectionHost();
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
