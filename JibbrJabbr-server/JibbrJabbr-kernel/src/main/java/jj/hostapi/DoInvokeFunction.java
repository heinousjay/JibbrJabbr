package jj.hostapi;

import jj.jqmessage.JQueryMessage;
import jj.script.CurrentScriptContext;
import jj.script.ScriptContextType;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class DoInvokeFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	public static final String PROP_DO_INVOKE = "//doInvoke";
	
	private final CurrentScriptContext context;
	
	public DoInvokeFunction(final CurrentScriptContext context) {
		this.context = context;
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
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot invoke remote functions without a connecton");
		}
		throw context.prepareContinuation(
			JQueryMessage.makeInvoke(String.valueOf(args[0]), String.valueOf(args[1]))
		);
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
