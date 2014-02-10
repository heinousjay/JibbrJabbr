package jj.engine;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.script.ScriptResourceType;
import jj.script.CurrentScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.slf4j.LoggerFactory;

@Singleton
class PrintfFunction extends BaseFunction implements HostObject {
	private static final long serialVersionUID = 1L;
	
	private static final String PRINTF = "printf";
	
	private static final Object[] EMPTY_ARGS = {};
	
	private final CurrentScriptEnvironment env;
	
	@Inject
	public PrintfFunction(final CurrentScriptEnvironment env) {
		this.env = env;
	}
	
	@Override
	public String name() {
		return PRINTF;
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
		if (args.length == 0) throw new IllegalArgumentException("bad arguments to printf");
		Object formatString = args[0];
		if (formatString == null) formatString = "<null>";
		if (formatString == Undefined.instance) formatString = "<undefined>";
		Object[] toFormat = 
			args.length > 1 ? 
				Arrays.asList(args).subList(1, args.length).toArray() :
				EMPTY_ARGS;

		LoggerFactory.getLogger(ScriptResourceType.Server.suffix(env.currentWebSocketConnectionHost().uri())).debug(
			String.format(String.valueOf(formatString), toFormat)
		);
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
