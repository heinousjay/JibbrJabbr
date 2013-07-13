package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptContext;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

@Singleton
class DoStoreFunction extends BaseFunction implements HostObject, ContributesScript {
	private static final long serialVersionUID = 1L;
	
	private static final String PROP_DO_STORE = "//doStore";
	
	private final CurrentScriptContext context;
	
	@Inject
	public DoStoreFunction(final CurrentScriptContext context) {
		this.context = context;
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
		if (context.connection() == null) {
			throw new IllegalStateException("cannot store remote info during " + context.httpRequest().state());
		}
		context.connection().send(JJMessage.makeStore(String.valueOf(args[0]), String.valueOf(args[1])));
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
