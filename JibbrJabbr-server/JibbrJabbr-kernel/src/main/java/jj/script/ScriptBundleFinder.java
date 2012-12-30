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
	
	public ScriptBundle forBaseName(String baseName) {
		return scriptBundles.get(baseName);
	}
	
	public ScriptBundle forBaseNameAndKey(String combinedKey) {
		ScriptBundle found = null;
		Matcher matcher = KEY_SPLITTER.matcher(combinedKey);
		if (matcher.matches()) {
			found = forBaseNameAndKey(matcher.group(1), matcher.group(2));
		}
		
		return found;
	}
	
	private ScriptBundle forBaseNameAndKey(String baseName, String key) {
		ScriptBundle found = null;
		ScriptBundle scriptBundle = scriptBundles.get(baseName);
		while (scriptBundle != null && found == null) {
			if (scriptBundle.sha1().equals(key)) {
				found = scriptBundle;
			} else {
				scriptBundle = scriptBundle.previous();
			}
		}
		return found;
	}

	public ScriptBundle forSocketUri(String socketUri) {
		ScriptBundle found = null;
		Matcher matcher = SOCKET.matcher(socketUri);
		if (matcher.matches()) {
			found = forBaseNameAndKey(matcher.group(1), matcher.group(2));
		}
		
		return found;
	}
	
	public ScriptBundle forSocketUriBaseName(String socketUri) {
		ScriptBundle found = null;
		Matcher matcher = SOCKET.matcher(socketUri);
		if (matcher.matches()) {
			String baseName = matcher.group(1);
			found = scriptBundles.get(baseName);
		}
		return found;
	}
	
}
