package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

@Singleton
class DoRetrieveFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	private static final String RETRIEVE = "retrieve";
	
	private final CurrentScriptEnvironment env;
	
	@Inject
	public DoRetrieveFunction(final CurrentScriptEnvironment context) {
		this.env = context;
	}
	
	@Override
	public String name() {
		return RETRIEVE;
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
		// TODO make this a type error
		if (args.length != 1) {
			throw new IllegalArgumentException("retrieve can only be called with a key");
		}
		// TODO, ensure we are connected to something
		throw env.prepareContinuation(
			JJMessage.makeRetrieve(String.valueOf(args[0]))
		);
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
