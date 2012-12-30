package jj.hostapi;

import jj.jqmessage.JQueryMessage;
import jj.script.CurrentScriptContext;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

class DoRetrieveFunction extends BaseFunction implements HostObject, ContributesScript {
	private static final long serialVersionUID = 1L;
	
	private static final String PROP_DO_RETRIEVE = "//doRetrieve";
	
	private final CurrentScriptContext context;
	
	public DoRetrieveFunction(final CurrentScriptContext context) {
		this.context = context;
	}
	
	@Override
	public String name() {
		return PROP_DO_RETRIEVE;
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
		return "function retrieve(key){return JSON.parse(global['" + PROP_DO_RETRIEVE + "'](key));}";
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (context.connection() == null) {
			throw new IllegalStateException("cannot retrieve remote info during " + context.httpRequest().state());
		}
		throw context.prepareContinuation(
			JQueryMessage.makeRetrieve(String.valueOf(args[0]))
		);
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
