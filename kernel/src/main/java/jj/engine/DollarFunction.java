package jj.engine;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.document.CurrentDocumentRequestProcessor;
import jj.document.DocumentScriptEnvironment;
import jj.http.server.websocket.CurrentWebSocketConnection;
import jj.jjmessage.JJMessage;
import jj.script.CurrentScriptEnvironment;

import org.jsoup.nodes.Element;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Implements the $ function host object, which is the primary script API to the document/client
 * @author jason
 *
 */
@Singleton
final class DollarFunction extends BaseFunction implements HostObject {

	// this is to match JQuery's element creation syntax and semantics
	private static final Pattern SIMPLE_ELEMENT_CREATION = Pattern.compile("^<(\\w+)\\s*\\/?>(?:<\\/\\1>|)$");
	private static final Pattern COMPLEX_ELEMENT_CREATION = Pattern.compile("^[^<]*(<[\\w\\W]+>)[^>]*$");

	private static final long serialVersionUID = 1L;
	
	private final CurrentWebSocketConnection connection;
	
	private final CurrentDocumentRequestProcessor document;
	
	private final CurrentScriptEnvironment env;
	
	@Inject
	public DollarFunction(
		final CurrentWebSocketConnection connection,
		final CurrentDocumentRequestProcessor document,
		final CurrentScriptEnvironment env
	) {
		this.connection = connection;
		this.document = document;
		this.env = env;
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
		
		DocumentScriptEnvironment dse = env.currentAs(DocumentScriptEnvironment.class);
		
		if (
			args.length == 1 && 
			(args[0] instanceof Function) && 
			dse.initializing() &&
			dse.getFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY) == null
		) {
			
			// this works in three modes - initial execution, it registers a function
			// document in scope, it's a selection API
			// connection in scope, it's a remote control
			
			dse.addFunction(DocumentScriptEnvironment.READY_FUNCTION_KEY, (Function)args[0]);
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
			if (document.current() != null) {
				result = new DocumentSelection(selector, document.currentDocument().select(selector), document, env);
			} else {
				result = new EventSelection(selector, connection, env);
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
			if (document.current() != null) {
				Element element = document.currentDocument().createElement(el);
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
				return new DocumentSelection(newSelection, element, document, env);
			}
			
			if (args != null && args.containsKey(ATTR_ID)) {
				// we can just return immediately, since we can make a unique selector here
				// so fire-and-forget style
				connection.current().send(JJMessage.makeInlineCreate(html, args));
				return new EventSelection(String.format("#%s", args.get(ATTR_ID)), connection, env);
			} else {
				throw env.preparedContinuation(JJMessage.makeCreate(html, args));
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