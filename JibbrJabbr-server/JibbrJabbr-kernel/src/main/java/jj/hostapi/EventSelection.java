package jj.hostapi;

import jj.jqmessage.JQueryMessage;
import jj.script.ContinuationState.Returns;
import jj.script.CurrentScriptContext;
import jj.script.EventNameHelper;
import jj.script.ScriptContextType;

import org.jsoup.nodes.Element;
import org.mozilla.javascript.Callable;

/**
 * Pretty much every operation in here fires out on the wire.
 * @author jason
 *
 */
public class EventSelection implements Selection {

	private final String selector;
	
	private final CurrentScriptContext context;
	
	private static final Returns returnsBoolean = new Returns() {

		@Override
		public Object transform(String value) {
			return "true".equals(value) ? Boolean.TRUE : Boolean.FALSE;
		}
		
	};
	
	public EventSelection(final String selector, final CurrentScriptContext context) {
		this.selector = selector;
		this.context = context;
	}
	
	@Override
	public String selector() {
		return selector;
	}
	
	@Override
	public Selection on(String type, Callable function) {
		return on(type, "", function);
	}
	
	@Override
	public Selection on(String type, String selector, Callable function) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot bind an event from this context");
		}
		context.connection().send(JQueryMessage.makeBind(this.selector, selector, type));
		context.associatedScriptBundle().addFunction(EventNameHelper.makeEventName(this.selector, selector, type), function);
		return this;
	}
	
	@Override
	public Selection bind(String type, boolean cancel) {
		return this;
	}
	@Override
	public Selection bind(String type, Callable function) {
		return on(type, function);
	}
	@Override
	public Selection bind(String type, Object data, boolean cancel) {
		return this;
	}
	@Override
	public Selection bind(String type, Object data, Callable function) {
		return on(type, function);
	}

	@Override
	public Selection click(Callable function) {
		return on("click", function);
	}
	
	@Override
	public Selection enter(Callable function) {
		return on("enter", function);
	}

	@Override
	public Selection data(String key, String value) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot set data from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "data", key, value));
		return this;
	}

	@Override
	public Selection text(String text) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot retrieve text from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "text", text));
		return this;
	}

	@Override
	public Selection append(Selection selection) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot append from this context");
		}
		context.connection().send(JQueryMessage.makeAppend(selector, selection.selector()));
		return this;
	}

	@Override
	public String attr(String attributeKey) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot get an attribute value from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "attr", attributeKey));
	}
	
	public String prop(String propKey) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot get an property value from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "prop", propKey));
	}

	@Override
	public boolean hasAttr(String attributeKey) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot check for an attribute value from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "hasAttr", attributeKey), returnsBoolean);
	}

	@Override
	public Selection attr(String attributeKey, String attributeValue) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot set an attribute from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "attr", attributeKey, attributeValue));
		return this;
	}

	public Selection prop(String propKey, String propValue) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot set an property from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "prop", propKey, propValue));
		return this;
	}

	@Override
	public Selection removeAttr(String attributeKey) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot remove an attribute from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "removeAttr", attributeKey));
		return this;
	}

	@Override
	public Selection addClass(String className) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot add a class from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "addClass", className));
		return this;
	}

	@Override
	public Selection removeClass(String className) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot remove a class from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "removeClass", className));
		return this;
	}

	@Override
	public Selection toggleClass(String className) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot toggle a class from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "toggleClass", className));
		return this;
	}

	@Override
	public boolean hasClass(String className) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot check for a class from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "hasClass", className), returnsBoolean);
	}

	@Override
	public String val() {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot retrieve a value from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "val"));
	}

	@Override
	public Selection val(String value) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot set a value from this context");
		}
		context.connection().send(JQueryMessage.makeSet(selector, "val", value));
		return this;
	}

	@Override
	public String text() {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot retrieve text from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "text"));
	}

	@Override
	public String html() {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot retrieve html from this context");
		}
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "html"));
	}

	@Override
	public Selection html(String html) {
		context.connection().send(JQueryMessage.makeSet(selector, "html", html));
		return this;
	}

	@Override
	public Selection prepend(String html) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection append(String html) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection before(String html) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection after(String html) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection wrap(String html) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection unwrap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection empty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection remove() {
		context.connection().send(JQueryMessage.makeSet(selector, "remove", ""));
		return this;
	}

	@Override
	public Selection select(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection not(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection eq(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean is(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Selection parents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element first() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element last() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return EventSelection.class.getSimpleName() + ": " + selector;
	}
}
