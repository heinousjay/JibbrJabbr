package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

@Singleton
public class DoInvokeFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DO_INVOKE = "//doInvoke";
	
	private final CurrentScriptEnvironment env;
	
	@Inject
	public DoInvokeFunction(final CurrentScriptEnvironment env) {
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
		// TODO assert that we're in a connection context
		throw env.preparedContinuation(
			JJMessage.makeInvoke(String.valueOf(args[0]), String.valueOf(args[1]))
		);
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
