package jj.hostapi;

import static jj.hostapi.PrepareConnectionIteratorFunction.*;

import java.util.Iterator;

import jj.script.CurrentScriptContext;
import jj.webbit.JJWebSocketConnection;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * the provider of the broadcast function, in concert with
 * PrepareConnectionIteratorFunction.
 * @author jason
 *
 */
class NextConnectionFunction extends BaseFunction implements HostObject, ContributesScript {
	private static final long serialVersionUID = 1L;
	
	private static final String NEXT_CONNECTION = "//nextConnection";
	
	private final CurrentScriptContext context;
	
	NextConnectionFunction(final CurrentScriptContext context) {
		this.context = context;
	}
	
	@Override
	public String name() {
		return NEXT_CONNECTION;
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
		return "function broadcast(func){global['" + PREPARE_CONNECTION_ITERATOR + "']();while(global['" + NEXT_CONNECTION + "']())func();}";
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		Object obj = context.scriptBundle().scope().get(PROP_CURRENT_ITERATOR, scope);
		if (!(obj instanceof Iterator<?>)) {
			// something really went wrong up in here
			throw new AssertionError("NextConnectionFunction called with no iterator.  something crossed up?");
		}
		
		@SuppressWarnings("unchecked")
		Iterator<JJWebSocketConnection> iterator = (Iterator<JJWebSocketConnection>)obj;
		Boolean needsFinish = (Boolean)context.scriptBundle().scope().get(PROP_ITERATOR_NEEDS_FINISH, scope);
		
		if (needsFinish != null && needsFinish) {
			context.end();
		}
		
		JJWebSocketConnection next = null;
		
		if (iterator.hasNext()) {
			next = iterator.next();
		}
		
		if (next != null) {
			context.initialize(next);
			ScriptableObject.putProperty(
				context.scriptBundle().scope(),
				PROP_ITERATOR_NEEDS_FINISH,
				Boolean.TRUE
			);
		} else {
			context.scriptBundle().scope().delete(PROP_CURRENT_ITERATOR);
			context.scriptBundle().scope().delete(PROP_ITERATOR_NEEDS_FINISH);
		}
		
		return next != null;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}
}
