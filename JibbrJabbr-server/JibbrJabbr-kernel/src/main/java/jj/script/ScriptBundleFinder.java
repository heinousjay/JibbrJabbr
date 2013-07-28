package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

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
	
	public AssociatedScriptBundle forBaseNameAndSha(String baseName, String key) {
		ScriptBundle scriptBundle = scriptBundles.get(baseName);
		return scriptBundle instanceof AssociatedScriptBundle && scriptBundle.sha1().equals(key) ?
			(AssociatedScriptBundle)scriptBundle :
			null;
	}
	
	public AssociatedScriptBundle forURIMatch(final URIMatch match) {
		return forBaseNameAndSha(match.name, match.sha1);
	}
	
}
