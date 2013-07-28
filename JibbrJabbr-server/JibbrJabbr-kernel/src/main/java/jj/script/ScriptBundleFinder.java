package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.StringUtils;
import jj.uri.URIMatch;

@Singleton
public class ScriptBundleFinder {
	
	private final ScriptBundles scriptBundles;
	
	@Inject
	ScriptBundleFinder(final ScriptBundles scriptBundles) {
		this.scriptBundles = scriptBundles;
	}
	
	public ModuleScriptBundle forBaseNameAndModuleIdentifier(final String baseName, final String moduleIdentifier) {
		String key = ModuleScriptBundle.makeKey(baseName, moduleIdentifier);
		ScriptBundle found = scriptBundles.get(key);
		return found instanceof ModuleScriptBundle ? (ModuleScriptBundle)found : null;
	}
	
	private ScriptBundle scriptBundleFor(String name) {
		ScriptBundle candidate = scriptBundles.get(name);
		if (candidate == null) {
			// if there is a dot in the name, remove the last one forward
			int lastDot = name.lastIndexOf('.');
			if (lastDot != -1) {
				candidate = scriptBundles.get(name.substring(0, lastDot));
			}
			
		}
		return candidate;
	}
	
	public AssociatedScriptBundle forURIMatch(final URIMatch match) {
		assert (!StringUtils.isEmpty(match.name)) : "can only find script bundles for uris with a base name";
		assert (!StringUtils.isEmpty(match.sha1))  : "can only find script bundles for uris with SHA1";
		
		ScriptBundle scriptBundle = scriptBundleFor(match.name);
		AssociatedScriptBundle result = null;
		if (scriptBundle instanceof AssociatedScriptBundle &&
			scriptBundle != null &&
			match.sha1.equals(scriptBundle.sha1())) {
			result = (AssociatedScriptBundle)scriptBundle;
		}
		
		return result;
	}
	
}
