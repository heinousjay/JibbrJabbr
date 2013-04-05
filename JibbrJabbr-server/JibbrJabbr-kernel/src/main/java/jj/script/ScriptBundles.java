package jj.script;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * IMPORTANT DO NOT MODIFY OUTSIDE SCRIPT THREADS
 * Simple concurrent hash map to hold script bundles,
 * key by the basename to the script bundle itself.
 * script bundles are further organized by key internally
 * @author jason
 *
 */
@Singleton
class ScriptBundles extends ConcurrentHashMap<String, ScriptBundle> {

	private static final long serialVersionUID = 1L;
	
	@Inject
	ScriptBundles() {
		// shouldn't really need more than four.  dunno yet.  need to tune?
		// well we can inject configuration later
		super(16, 0.75F, 4);
	}
}
