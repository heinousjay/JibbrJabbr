package jj.hostapi;

import java.util.Map;

import jj.jqmessage.JQueryMessage;
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
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> dataset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection text(String text) {
		context.connection().send(JQueryMessage.makeSet(selector, "text", text));
		return this;
	}

	@Override
	public Selection append(Selection selection) {
		context.connection().send(JQueryMessage.makeAppend(selector, selection.selector()));
		return this;
	}

	@Override
	public String attr(String attributeKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAttr(String attributeKey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Selection attr(String attributeKey, String attributeValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection removeAttr(String attributeKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection addClass(String className) {
		context.connection().send(JQueryMessage.makeSet(selector, "addClass", className));
		return this;
	}

	@Override
	public Selection removeClass(String className) {
		context.connection().send(JQueryMessage.makeSet(selector, "removeClass", className));
		return this;
	}

	@Override
	public Selection toggleClass(String className) {
		context.connection().send(JQueryMessage.makeSet(selector, "toggleClass", className));
		return this;
	}

	@Override
	public boolean hasClass(String className) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String val() {
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "val"));
	}

	@Override
	public Selection val(String value) {
		context.connection().send(JQueryMessage.makeSet(selector, "val", value));
		return this;
	}

	@Override
	public String text() {
		throw context.prepareContinuation(JQueryMessage.makeGet(selector, "text"));
	}

	@Override
	public boolean hasText() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String html() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String outerHtml() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection tagName(String tagName) {
		// TODO Auto-generated method stub
		return null;
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

}
