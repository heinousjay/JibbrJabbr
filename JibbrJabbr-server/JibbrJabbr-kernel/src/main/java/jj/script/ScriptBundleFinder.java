package jj.script;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptBundleFinder {
	
	private static final Pattern KEY_SPLITTER = Pattern.compile("^(.+?)/([a-f0-9]{40})$");
	private static final Pattern SOCKET = Pattern.compile("^/?(.+?)/([a-f0-9]{40}).socket$");
	
	private final ScriptBundles scriptBundles;
	
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
			found = forBaseNameAndKey(matcher.group(1), matcher.group(2));
		}
		
		return found;
	}
	
	private AssociatedScriptBundle forBaseNameAndKey(String baseName, String key) {
		ScriptBundle found = null;
		ScriptBundle scriptBundle = scriptBundles.get(baseName);
		while (scriptBundle != null && found == null) {
			if (scriptBundle.sha1().equals(key)) {
				found = scriptBundle;
			} else {
				found = null;
			}
		}
		return found instanceof AssociatedScriptBundle ? (AssociatedScriptBundle)found : null;
	}

	public AssociatedScriptBundle forSocketUri(String socketUri) {
		AssociatedScriptBundle found = null;
		Matcher matcher = SOCKET.matcher(socketUri);
		if (matcher.matches()) {
			found = forBaseNameAndKey(matcher.group(1), matcher.group(2));
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
