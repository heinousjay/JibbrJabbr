package jj.hostapi;


import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a JSON string into an object suitable to be
 * used in the script host.
 * 
 * this isn't a host object but it's a part of the script
 * host so it goes here.
 * @author jason
 *
 */
@Singleton
public class ScriptJSON {
	
	private final Logger log = LoggerFactory.getLogger(ScriptJSON.class);

	private final RhinoObjectCreator rhinoObjectCreator;
	
	@Inject
	public ScriptJSON(final RhinoObjectCreator rhinoObjectCreator) {
		this.rhinoObjectCreator = rhinoObjectCreator;
	}
	
	public Object parse(final String input) {
		// need to do some serious quote replacement
		String escaped = input.trim().replace("'", "\\'").replace("\"", "\\\"");
		Context context = rhinoObjectCreator.context();
		try {
			return context.evaluateString(
				rhinoObjectCreator.global(),
				"JSON.parse('" + escaped + "');",
				"ScriptJSON.parse",
				0,
				null
			);
		} catch (Exception e) {
			log.warn("couldn't parse {}", input);
			log.warn("", e);
			return null;
		} finally {
			Context.exit();
		}
	}
}
