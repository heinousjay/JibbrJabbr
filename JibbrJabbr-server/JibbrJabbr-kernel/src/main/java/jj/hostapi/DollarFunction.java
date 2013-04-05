package jj.hostapi;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.SelectorFormatException;
import jj.jqmessage.JQueryMessage;
import jj.script.CurrentScriptContext;
import jj.script.AssociatedScriptBundle;
import jj.script.ScriptRunner;
import org.jsoup.nodes.Element;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the $ function host object, which is the primary script API to the document/client
 * @author jason
 *
 */
@Singleton
final class DollarFunction extends BaseFunction implements HostObject {
	
	private static Logger log = LoggerFactory.getLogger(DollarFunction.class);

	// this is to match JQuery's element creation syntax and semantics
	private static final Pattern SIMPLE_ELEMENT_CREATION = Pattern.compile("^<(\\w+)\\s*\\/?>(?:<\\/\\1>|)$");
	private static final Pattern COMPLEX_ELEMENT_CREATION = Pattern.compile("^[^<]*(<[\\w\\W]+>)[^>]*$");

	private static final long serialVersionUID = 1L;
	
	private final CurrentScriptContext context;
	
	@Inject
	public DollarFunction(final CurrentScriptContext context) {
		this.context = context;
	}
	
	@Override
	public String name() {
		return "$";
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
		return name();
	}
	
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		throw new UnsupportedOperationException("$ does not support construction");
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		
		AssociatedScriptBundle scriptBundle = context.associatedScriptBundle();
		
		if (args.length == 1 && (args[0] instanceof Function)) {
			scriptBundle.addFunction(ScriptRunner.READY_FUNCTION_KEY, (Function)args[0]);
			return this; 
		}
		
		// Rhino uses something called a "ConsString" to delay string concatenations until they
		// are needed, so instead of a string directly, we just look for CharSequence and toString
		// it.  this lets us accept any String-like objects for selection
		// this bug was fun to track down :D
		if (args.length == 1 && (args[0] instanceof CharSequence)) {
			return select(((CharSequence)args[0]).toString());
		}
		
		if (args.length == 2 && (args[0] instanceof CharSequence) && (args[1] instanceof Map)) {
			return create(((CharSequence)args[0]).toString(), (Map<?, ?>)args[1]);
		}
		
		// warn that we are being called with something we don't recognize. see ConsString
		// comments above for the argument why :D
		log.warn("called with unrecognized argument set executing {}", scriptBundle.toUri());
		for (Object arg : args) {
			log.warn("{} of type {}", arg, arg == null ? "<null>" : arg.getClass());
		}
		// if nothing matches, we just return ourselves.  this allows silly constructs like
		// $()()()()()()()()()("body")
		// heh
		return this;
	}
	
	private Selection select(String selector) {
		// first try create
		Selection result = createInternal(selector, null);
		
		// then just select
		if (result == null) {
			if (context.connection() != null) {
				result = new EventSelection(selector, context);
			} else {
				result = new DocumentSelection(selector, context.document().select(selector), context);
			}
		}
		
		return result;
	}
	
	/**
	 * Two argument form of creation, with a "map of properties" to set 
	 * @param selector
	 * @param args
	 * @return
	 */
	private Selection create(String selector, Map<?,?> args) {
		Selection result = createInternal(selector, args);
		
		if (result == null) {
			// in this case we throw an exception because the selector
			// format was wrong for creation but these are creation args 
			throw new SelectorFormatException(selector);
		}
		
		return result;
	}
	
	private static final String ATTR_ID = "id";
	
	private Selection createInternal(String html, Map<?,?> args) {
		String el = checkSimpleCreation(html);
		if (el != null) {
			if (context.document() != null) {
				Element element = context.document().createElement(el);
				String id = null;
				if (args != null) {
					for (Object key : args.keySet()) {
						if (key != null && args.get(key) != null) {
							element.attr(String.valueOf(key), String.valueOf(args.get(key)));
							if ("id".equals(key)) {
								id = String.valueOf(args.get(key));
							}
						}
					}
				}
				String newSelection;
				if (id == null) {
					newSelection = html;
				} else {
					newSelection = "#" + id;
				}
				return new DocumentSelection(newSelection, element, context);
			}
			
			if (args.containsKey(ATTR_ID)) {
				// we can just return immediately, since we can make a unique selector here
				// so fire-and-forget style
				context.connection().send(JQueryMessage.makeInlineCreate(html, args));
				return new EventSelection(String.format("#%s", args.get(ATTR_ID)), context);
			} else {
				throw context.prepareContinuation(
					JQueryMessage.makeCreate(html, args));
			}
		}
		
		checkComplexCreation(html);
		
		return null;
	}

	
	/**
	 * For now, this is unsupported because JSoup doesn't support the
	 * same nodes in a collection as JQuery does.  TODO?
	 * @param selector
	 */
	private void checkComplexCreation(String selector) {
		Matcher matcher = COMPLEX_ELEMENT_CREATION.matcher(selector);
		if (matcher.matches()) {
		// we don't support this, jquery does some stuff with inner nodes we can't handle
			throw new SelectorFormatException(selector);
		}
	}
	
	private String checkSimpleCreation(String selector) {
		Matcher matcher = SIMPLE_ELEMENT_CREATION.matcher(selector);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
}