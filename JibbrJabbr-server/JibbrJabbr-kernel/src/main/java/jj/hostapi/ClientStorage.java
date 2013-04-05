package jj.hostapi;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.CurrentScriptContext;

import org.mozilla.javascript.Scriptable;

/**
 * a simple object to cache info against a given connection
 * 
 * for now, only available in the event phase
 * 
 * would be a great api for localStorage but you
 * can't do continuations in an assignment context :(
 * 
 * @author jason
 *
 */
@Singleton
class ClientStorage implements HostObject {
	
	private Map<String,Object> data() {
		return context.connection().clientStorage();
	}
	
	private final CurrentScriptContext context;
	
	@Inject
	ClientStorage(final CurrentScriptContext context) {
		this.context = context;
	}
	
	@Override
	public String name() {
		return "clientStorage";
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
	public String getClassName() {
		return name();
	}

	@Override
	public Object get(String name, Scriptable start) {
		return has(name, start) ? data().get(name) : NOT_FOUND;
	}

	@Override
	public Object get(int index, Scriptable start) {
		String key = String.valueOf(index);
		return has(key, start) ? data().get(key) : NOT_FOUND;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return data().containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return has(String.valueOf(index), start);
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		data().put(name, value);
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		data().put(String.valueOf(index), value);
	}

	@Override
	public void delete(String name) {
		data().remove(name);

	}

	@Override
	public void delete(int index) {
		data().remove(String.valueOf(index));

	}

	@Override
	public Scriptable getPrototype() {
		return null;
	}

	@Override
	public void setPrototype(Scriptable prototype) {
		// no.  asshole.
	}

	@Override
	public Scriptable getParentScope() {
		return null;
	}

	@Override
	public void setParentScope(Scriptable parent) {
		// no. asshole
	}

	@Override
	public Object[] getIds() {
		return data().keySet().toArray();
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		System.err.println("getDefaultValue");
		return this;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		return instance instanceof ClientStorage; 
	}

}
