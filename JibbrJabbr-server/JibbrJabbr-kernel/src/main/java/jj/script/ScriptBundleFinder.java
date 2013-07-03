package jj.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.servable.URIMatch;

@Singleton
public class ScriptBundleFinder {
	
	private static final Pattern KEY_SPLITTER = Pattern.compile("^([a-f0-9]{40})/(.+)$");
	private static final Pattern SOCKET = Pattern.compile("^/?([a-f0-9]{40})/(.+?).socket$");
	
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
	
	public AssociatedScriptBundle forBaseNameAndKey(String combinedKey) {
		AssociatedScriptBundle found = null;
		Matcher matcher = KEY_SPLITTER.matcher(combinedKey);
		if (matcher.matches()) {
			found = forBaseNameAndKey(matcher.group(2), matcher.group(1));
		}
		
		return found;
	}
	
	public AssociatedScriptBundle forBaseNameAndKey(String baseName, String key) {
		ScriptBundle scriptBundle = scriptBundles.get(baseName);
		return scriptBundle instanceof AssociatedScriptBundle && scriptBundle.sha1().equals(key) ?
			(AssociatedScriptBundle)scriptBundle :
			null;
	}
	
	public AssociatedScriptBundle forURIMatch(final URIMatch match) {
		return forBaseNameAndKey(match.name, match.sha);
	}

	public AssociatedScriptBundle forSocketUri(String socketUri) {
		AssociatedScriptBundle found = null;
		Matcher matcher = SOCKET.matcher(socketUri);
		if (matcher.matches()) {
			found = forBaseNameAndKey(matcher.group(2), matcher.group(1));
		}
		
		return found;
	}
	
	public AssociatedScriptBundle forSocketUriBaseName(String socketUri) {
		ScriptBundle found = null;
		Matcher matcher = SOCKET.matcher(socketUri);
		if (matcher.matches()) {
			String baseName = matcher.group(1);
			found = scriptBundles.get(baseName);
		}
		return found instanceof AssociatedScriptBundle ? (AssociatedScriptBundle)found : null;
	}
	
}
