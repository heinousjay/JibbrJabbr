package jj.script;

import java.util.HashMap;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import jj.SHA1Helper;
import jj.event.Publisher;
import jj.resource.ScriptResource;

/**
 * contains all of the information to execute a set of scripts associated with
 * an HTML document.
 * 
 * @author jason
 * 
 */
public class DocumentScriptExecutionEnvironment extends AbstractScriptExecutionEnvironment {

	private final ScriptResource clientScriptResource;

	private final ScriptResource sharedScriptResource;

	private final ScriptResource serverScriptResource;

	private final Scriptable scope;

	private final Script script;

	private final String sha1;

	private final String baseName;

	/**
	 * the callable functions found in this script organized by some sort of
	 * externally meaningful name
	 */
	private final HashMap<String, Callable> functions = new HashMap<>();

	DocumentScriptExecutionEnvironment(
		final Publisher publisher,
		final ScriptResource clientScriptResource,
		final ScriptResource sharedScriptResource,
		final ScriptResource serverScriptResource,
		final Scriptable scope,
		final Script script,
		final String baseName
	) {
		super(publisher);
		this.clientScriptResource = clientScriptResource;
		this.sharedScriptResource = sharedScriptResource;
		this.serverScriptResource = serverScriptResource;
		this.scope = scope;
		this.script = script;
		this.sha1 = SHA1Helper.keyFor(
			clientScriptResource == null ? null : clientScriptResource.sha1(),
			sharedScriptResource == null ? null : sharedScriptResource.sha1(),
			serverScriptResource == null ? null : serverScriptResource.sha1()
		);
		this.baseName = baseName;
	}

	public ScriptResource clientScriptResource() {
		return clientScriptResource;
	}

	public ScriptResource sharedScriptResource() {
		return sharedScriptResource;
	}

	public ScriptResource serverScriptResource() {
		return serverScriptResource;
	}

	@Override
	public Scriptable scope() {
		return scope;
	}

	@Override
	public Script script() {
		return script;
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public String baseName() {
		return baseName;
	}
	
	@Override
	public String scriptName() {
		return serverScriptResource.baseName();
	}

	public Callable getFunction(String name) {
		return functions.get(name);
	}

	public void addFunction(String name, Callable function) {
		functions.put(name, function);
	}
	
	public boolean removeFunction(String name) {
		return functions.remove(name) != null;
	}
	
	public boolean removeFunction(String name, Callable function) {
		return (functions.get(name) == function) && (functions.remove(name) == function);
	}

	public boolean equals(Object other) {
		return other instanceof DocumentScriptExecutionEnvironment && 
			((DocumentScriptExecutionEnvironment)other).toUri().equals(toUri());
	}

	public int hashCode() {
		return toUri().hashCode();
	}

	public String toUri() {
		return "/" + sha1 + "/" + baseName;
	}

	public String toSocketUri() {
		return toUri() + ".socket";
	}
}
