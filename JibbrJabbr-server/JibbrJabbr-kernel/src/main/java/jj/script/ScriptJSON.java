package jj.script;


import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a JSON string into an object suitable to be
 * used in the script engine
 * @author jason
 *
 */
@Singleton
public class ScriptJSON {
	
	private final Logger log = LoggerFactory.getLogger(ScriptJSON.class);
	
	private final RhinoContextMaker contextMaker;
	
	@Inject
	public ScriptJSON(final RhinoContextMaker contextMaker) {
		this.contextMaker = contextMaker;
	}
	
	private String emptyOrInput(final String input) {
		return input == null ? "" : input;
	}
	
	public Object parse(final String input) {
		// need to do some serious quote replacement
		String escaped = emptyOrInput(input).trim().replace("'", "\\'").replace("\"", "\\\"");
		
		try (RhinoContext context = contextMaker.context()) {
			return context.evaluateString(contextMaker.generalScope(), "JSON.parse('" + escaped + "');", "ScriptJSON.parse");
		} catch (Exception e) {
			log.warn("couldn't JSON.parse {}", input);
			log.warn("", e);
			return null;
		}
	}
}