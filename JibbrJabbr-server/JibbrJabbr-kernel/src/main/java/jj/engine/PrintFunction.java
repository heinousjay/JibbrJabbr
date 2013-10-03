package jj.engine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.CurrentScriptContext;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.slf4j.LoggerFactory;

@Singleton
class PrintFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	private static final String PRINT = "print";

	private final CurrentScriptContext context;
	
	@Inject
	public PrintFunction(final CurrentScriptContext context) {
		this.context = context;
	}
	
	@Override
	public String name() {
		return PRINT;
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
		if (args.length != 1) throw new IllegalArgumentException("bad arguments to print");
		Object printString = args[0];
		if (printString == null) printString = "<null>";
		if (printString == Undefined.instance) printString = "<undefined>";
		LoggerFactory.getLogger(context.scriptEnvironment().scriptName()).debug("{}", printString);
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
