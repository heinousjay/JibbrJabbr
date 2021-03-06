package jj.engine;

import java.util.Collections;
import java.util.Map;

import jj.document.CurrentDocumentRequestProcessor;
import jj.http.server.websocket.WebSocketConnectionHost;
import jj.jjmessage.EventNameHelper;
import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;


/**
 * The Selection object when the document is being processed.
 * @author jason
 *
 */
public class DocumentSelection implements Selection {
	
	
	private final Elements elements;
	
	private final String selector;
	
	private final CurrentDocumentRequestProcessor document;
	
	private final CurrentScriptEnvironment env;
	
	public DocumentSelection(final String selector, final Element element, final CurrentDocumentRequestProcessor document, final CurrentScriptEnvironment env) {
		this(selector, new Elements(element), document, env);
	}
	
	public DocumentSelection(final String selector, final Elements elements, final CurrentDocumentRequestProcessor document, final CurrentScriptEnvironment env) {
		this.selector = selector;
		this.elements = elements;
		this.document = document;
		this.env = env;
	}
	
	
	public String selector() {
		return selector;
	}
	
	@Override
	public Selection hide() {
		document.current().addStartupJJMessage(JJMessage.makeSet(this.selector, "hide", null));
		return this;
	}

	@Override
	public Selection hide(final String duration) {
		document.current().addStartupJJMessage(JJMessage.makeSet(this.selector, "hide", duration));
		return this;
	}

	@Override
	public Selection show() {
		document.current().addStartupJJMessage(JJMessage.makeSet(this.selector, "show", null));
		return this;
	}

	@Override
	public Selection show(final String duration) {
		document.current().addStartupJJMessage(JJMessage.makeSet(this.selector, "show", duration));
		return this;
	}
	
	// -- Events
	@Override
	public Selection on(final String type, final Callable function) {
		return on(type, "", function);
	}
	
	@Override
	public Selection on(final String type, final String selector, final Callable function) {
		document.current().addStartupJJMessage(JJMessage.makeBind(this.selector, selector, type));
		// this is wrong! we need a way to associate the clients!
		// also UGLY UGLY UGLY.  but this class is going away.  maybe today!
		((WebSocketConnectionHost)env.current()).addFunction(EventNameHelper.makeEventName(this.selector, selector, type), function);
		return this;
	}
	
	@Override
	public Selection bind(final String type, final Callable function) {
		return on(type, function);
	}
	
	@Override
	public Selection bind(final String type, final Object data, final Callable function) {
		return on(type, function);
	}
	
	@Override
	public Selection bind(String type, boolean cancel) {
		// TODO!
		return this;
	}
	
	@Override
	public Selection bind(String type, Object data, boolean cancel) {
		// TODO make this work
		return this;
	}
	
	@Override
	public Selection click(Callable function) {
		return bind("click", null, function);
	}
	
	@Override
	public Selection enter(Callable function) {
		return bind("enter", null, function);
	};
	
	
	public Selection append(final Selection selection) {
		// it will always be
		DocumentSelection in = (DocumentSelection)selection;
		Elements clone = null;
		for (final Element dst : elements) {
			clone = clone == null ? in.elements : in.elements.clone();
			for (final Element src : clone) {
				dst.appendChild(src);
			}
		}
		return this;
	}
	
	public Selection text(String text) {
		for (final Element element : elements) {
			element.text(text);
		}
		return this;
	}
	
	public String data(final String key) {
		for (final Element element : elements) {
			if (element.dataset().containsKey(key)) {
				return element.dataset().get(key);
			}
		}
		
		return null;
	}
	
	public Selection data(final String key, final String value) {
		for (final Element element : elements) {
			element.dataset().put(key, value);
		}
		return this;
	}
	
	public Map<String, String> dataset() {
		Element first = first();
		return first == null ? Collections.<String, String>emptyMap() : first.dataset();
	}

	@Override
	public String attr(String attributeKey) {
		return elements.attr(attributeKey);
	}

	@Override
	public boolean hasAttr(String attributeKey) {
		return elements.hasAttr(attributeKey);
	}

	@Override
	public Selection attr(String attributeKey, String attributeValue) {
		elements.attr(attributeKey, attributeValue);
		return this;
	}

	@Override
	public Selection removeAttr(String attributeKey) {
		elements.removeAttr(attributeKey);
		return this;
	}

	@Override
	public Selection addClass(String className) {
		elements.addClass(className);
		return this;
	}

	@Override
	public Selection removeClass(String className) {
		elements.removeClass(className);
		return this;
	}

	@Override
	public Selection toggleClass(String className) {
		elements.toggleClass(className);
		return this;
	}

	@Override
	public boolean hasClass(String className) {
		return elements.hasClass(className);
	}

	@Override
	public String val() {
		return elements.val();
	}

	@Override
	public Selection val(String value) {
		elements.val(value);
		return this;
	}

	@Override
	public String text() {
		return elements.text();
	}

	public boolean hasText() {
		return elements.hasText();
	}

	@Override
	public Object html() {
		return Context.javaToJS(elements.html(), env.current().scope());
	}

	public String outerHtml() {
		return elements.outerHtml();
	}

	public Selection tagName(String tagName) {
		elements.tagName(tagName);
		return this;
	}

	@Override
	public Selection html(String html) {
		elements.html(html);
		return this;
	}

	@Override
	public Selection prepend(String html) {
		elements.prepend(html);
		return this;
	}

	@Override
	public Selection append(String html) {
		elements.append(html);
		return this;
	}

	@Override
	public Selection before(String html) {
		elements.before(html);
		return this;
	}

	@Override
	public Selection after(String html) {
		elements.after(html);
		return this;
	}

	@Override
	public Selection wrap(String html) {
		elements.wrap(html);
		return this;
	}

	@Override
	public Selection unwrap() {
		elements.unwrap();
		return this;
	}

	@Override
	public Selection empty() {
		elements.empty();
		return this;
	}

	@Override
	public Selection remove() {
		elements.remove();
		return this;
	}

	@Override
	public Selection select(String query) {
		// FIXME this selector is not correct at all
		return new DocumentSelection(selector, elements.select(query), document, env);
	}

	@Override
	public Selection not(String query) {
		// FIXME this selector is not correct at all
		return new DocumentSelection(selector, elements.not(query), document, env);
	}

	@Override
	public Selection eq(int index) {
		// FIXME this selector is not correct at all
		return new DocumentSelection(selector, elements.eq(index), document, env);
	}

	@Override
	public boolean is(String query) {
		return elements.is(query);
	}

	@Override
	public Selection parents() {
		// FIXME this selector is not correct at all
		return new DocumentSelection(selector, elements.parents(), document, env);
	}

	@Override
	public Element first() {
		return elements.first();
	}

	@Override
	public Element last() {
		return elements.last();
	}
	
	@Override
	public Selection clone() {
		return new DocumentSelection(selector, elements.clone(), document, env);
	}

	@Override
	public String toString() {
		return DocumentSelection.class.getSimpleName() + ": " + selector + "\n" + elements;
	}
}
