package jj.hostapi;


import java.util.Set;

import jj.script.CurrentScriptContext;
import jj.webbit.JJWebSocketConnection;
import jj.webbit.WebSocketConnections;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PrepareConnectionIteratorFunction extends BaseFunction implements HostObject {
	
	static final String PREPARE_CONNECTION_ITERATOR = "//prepareConnectionIteration";
	static final String PROP_CURRENT_ITERATOR = "//currentIterator";
	static final String PROP_ITERATOR_NEEDS_FINISH = "//iteratorNeedsFinish";
	
	private static final long serialVersionUID = 1L;
	
	private final Logger log = LoggerFactory.getLogger(PrepareConnectionIteratorFunction.class);
	
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
		
		log.debug("preparing to broadcast for {}", context.associatedScriptBundle());
		
		Set<JJWebSocketConnection> connectionSet = connections.forScript(context.associatedScriptBundle());
		
		log.debug("connections are {}", connectionSet);
		
		ScriptableObject.putProperty(
			context.associatedScriptBundle().scope(),
			PROP_CURRENT_ITERATOR,
			connectionSet.iterator()
		);
		ScriptableObject.putProperty(
			context.associatedScriptBundle().scope(),
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
