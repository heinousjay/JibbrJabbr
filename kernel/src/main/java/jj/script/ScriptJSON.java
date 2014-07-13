package jj.script;


import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.mozilla.javascript.ScriptableObject;

/**
 * Parses a JSON string into an object suitable to be
 * used in the script engine
 * @author jason
 *
 */
@Singleton
public class ScriptJSON {
	
	private final Provider<RhinoContext> contextProvider;
	
	private final ScriptableObject scope;
	
	@Inject
	public ScriptJSON(final Provider<RhinoContext> contextProvider) {
		this.contextProvider = contextProvider;
		// for the sake of safety, incoming strings are parsed
		// against an independent scope
		// i don't know if it helps but it can't hurt
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
			// TODO maybe an event here? this can happen if people are feeding us crap
			// and logging that may or may not be helpful
			return null;
		}
	}
}
