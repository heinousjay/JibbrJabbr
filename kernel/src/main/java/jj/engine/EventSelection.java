package jj.engine;

import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptContext;
import jj.script.CurrentScriptEnvironment;
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
	
	private final CurrentScriptEnvironment env;
	
	public EventSelection(final String selector, final CurrentScriptContext context, final CurrentScriptEnvironment env) {
		this.selector = selector;
		this.context = context;
		this.env = env;
	}
	
	private void verifyContext(final String actionDescription) {
		if (context.type() != ScriptContextType.WebSocket) {
			throw new IllegalStateException("cannot " + actionDescription + " from this context");
		}
	}
	
	@Override
	public String selector() {
		return selector;
	}
	
	@Override
	public Selection hide() {
		verifyContext("hide a selection");
		context.connection().send(JJMessage.makeSet(this.selector, "hide", null));
		return this;
	}

	@Override
	public Selection hide(final String duration) {
		verifyContext("hide a selection");
		context.connection().send(JJMessage.makeSet(this.selector, "hide", duration));
		return this;
	}

	@Override
	public Selection show() {
		verifyContext("show a selection");
		context.connection().send(JJMessage.makeSet(this.selector, "show", null));
		return this;
	}

	@Override
	public Selection show(final String duration) {
		verifyContext("show a selection");
		context.connection().send(JJMessage.makeSet(this.selector, "show", duration));
		return this;
	}
	
	@Override
	public Selection on(String type, Callable function) {
		return on(type, "", function);
	}
	
	@Override
	public Selection on(String type, String selector, Callable function) {
		verifyContext("bind an event");
		context.connection().send(JJMessage.makeBind(this.selector, selector, type));
		context.connection().addFunction(EventNameHelper.makeEventName(this.selector, selector, type), function);
		return this;
	}
	
	public Selection off(String type) {
		return off(type, "");
	}
	
	public Selection off(String type, String selector) {
		verifyContext("unbind an event");
		if (context.connection().removeFunction(EventNameHelper.makeEventName(this.selector, selector, type))) {
			context.connection().send(JJMessage.makeUnbind(this.selector, selector, type));
		}
		return this;
		
	}
	
	public Selection off(String type, Callable function) {
		return off(type, "", function);
	}
	
	public Selection off(String type, String selector, Callable function) {
		verifyContext("unbind an event");
		if (context.connection().removeFunction(EventNameHelper.makeEventName(this.selector, selector, type), function)) {
			context.connection().send(JJMessage.makeUnbind(this.selector, selector, type));
		}
		return this;
	}
	
	@Override
	public Selection bind(String type, boolean cancel) {
		throw new UnsupportedOperationException("bind with autocancel is not yet implemented");
	}
	@Override
	public Selection bind(String type, Callable function) {
		return on(type, function);
	}
	@Override
	public Selection bind(String type, Object data, boolean cancel) {
		throw new UnsupportedOperationException("bind with autocancel is not yet implemented");
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
	
	public String data(String key) {
		verifyContext("get data");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "data", key));
	}

	@Override
	public Selection data(String key, String value) {
		verifyContext("set data");
		context.connection().send(JJMessage.makeSet(selector, "data", key, value));
		return this;
	}

	@Override
	public Selection text(String text) {
		verifyContext("retrieve text");
		context.connection().send(JJMessage.makeSet(selector, "text", text));
		return this;
	}

	@Override
	public Selection append(Selection selection) {
		verifyContext("append");
		context.connection().send(JJMessage.makeAppend(selector, selection.selector()));
		return this;
	}

	@Override
	public String attr(String attributeKey) {
		verifyContext("get an attribute value");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "attr", attributeKey));
	}
	
	public String prop(String propKey) {
		verifyContext("get an property value");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "prop", propKey));
	}

	@Override
	public boolean hasAttr(String attributeKey) {
		verifyContext("check for an attribute");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "hasAttr", attributeKey));
	}

	@Override
	public Selection attr(String attributeKey, String attributeValue) {
		verifyContext("set an attribute");
		context.connection().send(JJMessage.makeSet(selector, "attr", attributeKey, attributeValue));
		return this;
	}

	public Selection prop(String propKey, String propValue) {
		verifyContext("set an property");
		context.connection().send(JJMessage.makeSet(selector, "prop", propKey, propValue));
		return this;
	}

	@Override
	public Selection removeAttr(String attributeKey) {
		verifyContext("remove an attribute");
		context.connection().send(JJMessage.makeSet(selector, "removeAttr", attributeKey));
		return this;
	}

	@Override
	public Selection addClass(String className) {
		verifyContext("add a class");
		context.connection().send(JJMessage.makeSet(selector, "addClass", className));
		return this;
	}

	@Override
	public Selection removeClass(String className) {
		verifyContext("remove a class");
		context.connection().send(JJMessage.makeSet(selector, "removeClass", className));
		return this;
	}

	@Override
	public Selection toggleClass(String className) {
		verifyContext("toggle a class");
		context.connection().send(JJMessage.makeSet(selector, "toggleClass", className));
		return this;
	}

	@Override
	public boolean hasClass(String className) {
		verifyContext("check for a class");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "hasClass", className));
	}

	@Override
	public String val() {
		verifyContext("retrieve a value");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "val"));
	}

	@Override
	public Selection val(String value) {
		verifyContext("set a value");
		context.connection().send(JJMessage.makeSet(selector, "val", value));
		return this;
	}

	@Override
	public String text() {
		verifyContext("retrieve text");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "text"));
	}

	@Override
	public String html() {
		verifyContext("retrieve html");
		throw env.prepareContinuation(JJMessage.makeGet(selector, "html"));
	}

	@Override
	public Selection html(String html) {
		verifyContext("set html");
		context.connection().send(JJMessage.makeSet(selector, "html", html));
		return this;
	}

	@Override
	public Selection prepend(String html) {
		verifyContext("prepend html");
		context.connection().send(JJMessage.makeSet(selector, "prepend", html));
		return this;
	}

	@Override
	public Selection append(String html) {
		verifyContext("append html");
		context.connection().send(JJMessage.makeSet(selector, "append", html));
		return this;
	}

	@Override
	public Selection before(String html) {
		verifyContext("set html before");
		context.connection().send(JJMessage.makeSet(selector, "before", html));
		return this;
	}

	@Override
	public Selection after(String html) {
		verifyContext("set html after");
		context.connection().send(JJMessage.makeSet(selector, "after", html));
		return this;
	}

	@Override
	public Selection wrap(String html) {
		verifyContext("wrap elements with html");
		context.connection().send(JJMessage.makeSet(selector, "wrap", html));
		return this;
	}

	@Override
	public Selection unwrap() {
		verifyContext("unwrap elements");
		context.connection().send(JJMessage.makeSet(selector, "unwrap", ""));
		return this;
	}

	@Override
	public Selection empty() {
		verifyContext("empty an element set");
		context.connection().send(JJMessage.makeSet(selector, "empty", ""));
		return this;
	}

	@Override
	public Selection remove() {
		verifyContext("remove elements");
		context.connection().send(JJMessage.makeSet(selector, "remove", ""));
		return this;
	}

	@Override
	public Selection select(String query) {
		// this i believe is exactly what jquery does!
		return new EventSelection(selector + " " + query, context, env);
	}

	@Override
	public Selection not(String query) {
		throw new UnsupportedOperationException("not is not yet implemented");
	}

	@Override
	public Selection eq(int index) {
		throw new UnsupportedOperationException("eq is not yet implemented");
	}

	@Override
	public boolean is(String query) {
		throw new UnsupportedOperationException("is is not yet implemented (and may never be!)");
	}

	@Override
	public Selection parents() {
		throw new UnsupportedOperationException("parents is not yet implemented");
	}

	@Override
	public Element first() {
		throw new UnsupportedOperationException("first is not yet implemented and probably will never be");
	}

	@Override
	public Element last() {
		throw new UnsupportedOperationException("first is not yet implemented and probably will never be");
	}
	
	@Override
	public Selection clone() {
		return new EventSelection(selector, context, env);
	}

	@Override
	public String toString() {
		return EventSelection.class.getSimpleName() + ": " + selector;
	}
}
