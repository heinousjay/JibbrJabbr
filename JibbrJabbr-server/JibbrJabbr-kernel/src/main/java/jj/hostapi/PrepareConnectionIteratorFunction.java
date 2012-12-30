package jj.hostapi;


import jj.script.CurrentScriptContext;
import jj.webbit.WebSocketConnections;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

class PrepareConnectionIteratorFunction extends BaseFunction implements HostObject {
	
	static final String PREPARE_CONNECTION_ITERATOR = "//prepareConnectionIteration";
	static final String PROP_CURRENT_ITERATOR = "//currentIterator";
	static final String PROP_ITERATOR_NEEDS_FINISH = "//iteratorNeedsFinish";
	
	private static final long serialVersionUID = 1L;
	
	private final CurrentScriptContext context;
	
	private final WebSocketConnections connections;
	
	PrepareConnectionIteratorFunction(
		final CurrentScriptContext context,
		final WebSocketConnections connections
	) {
		this.context = context;
		this.connections = connections;
	}

	@Override
	public String name() {
		return PREPARE_CONNECTION_ITERATOR;
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
			throw new IllegalStateException("cannot broadcast during " + context.httpRequest().state());
		}
		
		ScriptableObject.putProperty(
			context.scriptBundle().scope(),
			PROP_CURRENT_ITERATOR,
			connections.forScript(context.scriptBundle()).iterator()
		);
		ScriptableObject.putProperty(
			context.scriptBundle().scope(),
			PROP_ITERATOR_NEEDS_FINISH,
			Boolean.FALSE
		);
		return Undefined.instance;
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException();
	}

}
