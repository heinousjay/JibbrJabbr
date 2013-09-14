package jj.script;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * IMPORTANT DO NOT MODIFY OUTSIDE SCRIPT THREADS
 * Simple concurrent hash map to hold script execution environments,
 * key by the basename to the script execution environment itself.
 * script execution environments are further organized by key internally
 * @author jason
 *
 */
@Singleton
class ScriptExecutionEnvironments extends ConcurrentHashMap<String, ScriptExecutionEnvironment> {

	private static final long serialVersionUID = 1L;
	
	@Inject
	ScriptExecutionEnvironments() {
		// shouldn't really need more than four.  dunno yet.  need to tune?
		// well we can inject configuration later
		super(16, 0.75F, 4);
	}
}
