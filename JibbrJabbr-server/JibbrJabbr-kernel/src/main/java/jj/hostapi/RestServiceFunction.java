package jj.hostapi;


import javax.inject.Inject;
import javax.inject.Singleton;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


/**
 * Host object to provide basic configuration of the
 * common elements of a REST service.
 * @author jason
 *
 */
@Singleton
class RestServiceFunction extends BaseFunction implements HostObject {
	
	private static final long serialVersionUID = 1L;
	
	private static final String OPERATIONS = "operations";
	private static final String BASE_URL = "baseUrl";
	
	private final RestCallProvider restCallProvider;
	
	@Inject
	RestServiceFunction(final RestCallProvider restCallProvider) {
		this.restCallProvider = restCallProvider;
	}
	
	@Override
	public String name() {
		return "RestService";
	}
	
	@Override
	public boolean constant() {
		return true;
	}
	
	@Override
	public boolean readonly() {
		return true;
	}
	
	@Override
	public boolean permanent() {
		return true;
	}
	
	@Override
	public boolean dontenum() {
		return true;
	}
	
	@Override
	public int getArity() {
		return 1;
	}
	
	@Override
	public int getLength() {
		return 1;
	}
	
	@Override
	public String getFunctionName() {
		return name();
	}
	
	@Override
	public Scriptable createObject(Context cx, Scriptable scope) {
		// shortcutting base class construction
		return null;
	}
	
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
		// we always construct a new object
		if (args.length != 1 || !(args[0] instanceof Scriptable)) {
			throw new IllegalArgumentException("");
		}
		Scriptable configuration = (Scriptable)args[0];
		Scriptable instance = super.createObject(cx, scope);
		
		createRestService(instance, configuration);
		
		return instance;
	}
	
	/**
	 * takes a scriptable object and adds the
	 * Rest service functionality according to
	 * the options
	 * @param instance
	 */
	private void createRestService(Scriptable instance, Scriptable configuration) {
		String baseUrl = 
			(String)Context.jsToJava(ScriptableObject.getProperty(configuration, BASE_URL), String.class);
		
		if (baseUrl == null) {
			throw new IllegalArgumentException("baseUrl is required");
		}
		
		ScriptableObject.defineProperty(instance, BASE_URL, baseUrl, CONST);
		
		Scriptable operations = (Scriptable)ScriptableObject.getProperty(configuration, OPERATIONS);
		
		for (Object idObj : operations.getIds()) {
			String id = String.valueOf(idObj);
			Object operation = ScriptableObject.getProperty(operations, id);
			// could be true
			if (Boolean.TRUE.equals(operation)) {
				operation = null;
			} else if (!(operation instanceof Scriptable)) {
				throw new IllegalArgumentException("invalid format for operations");
			}
			
			RestCallOptions options = new RestCallOptions((Scriptable)operation);
			Function restCall = restCallProvider.createRestCall(options);
			
			ScriptableObject.defineProperty(instance, id, restCall, EMPTY);
		}
	}
}