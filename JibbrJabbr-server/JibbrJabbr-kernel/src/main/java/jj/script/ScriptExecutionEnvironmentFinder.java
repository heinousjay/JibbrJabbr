package jj.script;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.StringUtils;
import jj.uri.URIMatch;

@Singleton
public class ScriptExecutionEnvironmentFinder {
	
	private final ScriptExecutionEnvironments scriptExecutionEnvironments;
	
	@Inject
	ScriptExecutionEnvironmentFinder(final ScriptExecutionEnvironments scriptExecutionEnvironments) {
		this.scriptExecutionEnvironments = scriptExecutionEnvironments;
	}
	
	public ModuleScriptExecutionEnvironment forBaseNameAndModuleIdentifier(final String baseName, final String moduleIdentifier) {
		String key = ModuleScriptExecutionEnvironment.makeKey(baseName, moduleIdentifier);
		ScriptExecutionEnvironment found = scriptExecutionEnvironments.get(key);
		return found instanceof ModuleScriptExecutionEnvironment ? (ModuleScriptExecutionEnvironment)found : null;
	}
	
	private ScriptExecutionEnvironment scriptExecutionEnvironmentFor(String name) {
		ScriptExecutionEnvironment candidate = scriptExecutionEnvironments.get(name);
		if (candidate == null) {
			// if there is a dot in the name, remove the last one forward
			int lastDot = name.lastIndexOf('.');
			if (lastDot != -1) {
				candidate = scriptExecutionEnvironments.get(name.substring(0, lastDot));
			}
			
		}
		return candidate;
	}
	
	public DocumentScriptExecutionEnvironment forURIMatch(final URIMatch match) {
		assert (!StringUtils.isEmpty(match.name)) : "can only find script execution environments for uris with a base name";
		assert (!StringUtils.isEmpty(match.sha1))  : "can only find script execution environments for uris with SHA1";
		
		ScriptExecutionEnvironment scriptExecutionEnvironment = scriptExecutionEnvironmentFor(match.name);
		DocumentScriptExecutionEnvironment result = null;
		if (scriptExecutionEnvironment instanceof DocumentScriptExecutionEnvironment &&
			scriptExecutionEnvironment != null &&
			match.sha1.equals(scriptExecutionEnvironment.sha1())) {
			result = (DocumentScriptExecutionEnvironment)scriptExecutionEnvironment;
		}
		
		return result;
	}
	
}
