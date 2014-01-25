package jj.script;


import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.ScriptableObject;
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
	
	private final Provider<RhinoContext> contextProvider;
	
	private final ScriptableObject scope;
	
	@Inject
	public ScriptJSON(final Provider<RhinoContext> contextProvider) {
		this.contextProvider = contextProvider;
		try (RhinoContext context = contextProvider.get()) {
			scope = context.initStandardObjects(true);
		}
	}
	
	private String emptyOrInput(final String input) {
		return input == null ? "" : input;
	}
	
	public Object parse(final String input) {
		
		try (RhinoContext context = contextProvider.get()) {
			return context.newJsonParser(scope).parseValue(emptyOrInput(input).trim());
		} catch (Exception e) {
			//log.warn("couldn't JSON.parse {}", input);
			log.warn("", e);
			return null;
		}
	}
}
