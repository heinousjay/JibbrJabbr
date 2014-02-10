package jj.engine;


import jj.script.CurrentScriptEnvironment;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * handles general event registrations as a host object. to
 * create a new event handler host object, add the name of 
 * the event to the {@link HostEvent} enumeration.
 * @author jason
 *
 */
abstract class EventRegistrationFunction extends BaseFunction implements HostObject {

	private static final long serialVersionUID = 1L;
	
	private final HostEvent hostEvent;
	
	private final CurrentScriptEnvironment env;
	
	/**
	 * create a new instance of this function
	 * the function name determines what bucket
	 * of listeners we add our arguments to.  also
	 * determines what we respond with from getFunctionName();
	 * @param functionName
	 */
	protected EventRegistrationFunction(final HostEvent hostEvent, final CurrentScriptEnvironment env) {
		this.hostEvent = hostEvent;
		this.env = env;
	}
	
	@Override
	public String name() {
		return hostEvent.toString();
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
	public String getFunctionName() {
		return hostEvent.toString();
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		if (args.length != 1 || !(args[0] instanceof Callable)) {
			throw new IllegalArgumentException(String.format("%s takes only one argument of type function", hostEvent));
		}
		
		env.currentWebSocketConnectionHost().addFunction(hostEvent.toString(), (Callable)args[0]);
		
		// nothing worth returning here, chaining doesn't make sense
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException("");
	}
	
}
