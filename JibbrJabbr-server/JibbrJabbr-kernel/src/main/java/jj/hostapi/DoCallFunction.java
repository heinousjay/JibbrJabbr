package jj.hostapi;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.jqmessage.JQueryMessage;
import jj.script.CurrentScriptContext;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

@Singleton
public class DoCallFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DO_CALL = "//doCall";
	
	private final CurrentScriptContext context;
	
	@Inject
	public DoCallFunction(final CurrentScriptContext context) {
		this.context = context;
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
		if (context.connection() == null) {
			throw new IllegalStateException("cannot call remote functions during " + context.httpRequest().state());
		}
		context.connection().send(JQueryMessage.makeCall(String.valueOf(args[0]), String.valueOf(args[1])));
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
