/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.script.Global;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * <p>
 * Exposes the environment variables of the process to the script API
 * 
 * <p>
 * may make this a per-request instance? in which case allow the prototype
 * to be altered?
 * @author jason
 *
 */
@Singleton
public class EnvironmentScriptable implements Scriptable {
	
	private final Scriptable parentScope;
	private final Scriptable prototype;
	
	@Inject
	EnvironmentScriptable(final @Global ScriptableObject global) {
		parentScope = global;
		prototype = NativeObject.getObjectPrototype(parentScope);
	}

	@Override
	public String getClassName() {
		return getClass().getSimpleName();
	}

	@Override
	public Object get(String name, Scriptable start) {
		String result = System.getenv(name);
		return result == null ? NOT_FOUND : result;
	}

	@Override
	public Object get(int index, Scriptable start) {
		return NOT_FOUND;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return System.getenv(name) != null;
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return false;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		// nosir
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		// nosir
	}

	@Override
	public void delete(String name) {
		// nosir
	}

	@Override
	public void delete(int index) {
		// nosir
	}

	@Override
	public Scriptable getPrototype() {
		return prototype;
	}

	@Override
	public void setPrototype(Scriptable prototype) {
		// nope
	}

	@Override
	public Scriptable getParentScope() {
		return parentScope;
	}

	@Override
	public void setParentScope(Scriptable parent) {
		// nope
	}

	@Override
	public Object[] getIds() {
		return System.getenv().keySet().toArray();
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		return this;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		return instance == this;
	}

}
