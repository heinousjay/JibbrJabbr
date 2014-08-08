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
package jj.i18n;

import java.util.Set;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

class MessagesScriptable implements Scriptable {
	
	private final MessagesResource resource;
	private Scriptable parentScope;
	private Scriptable prototype;
	
	MessagesScriptable(final MessagesResource resource, final Scriptable parentScope) {
		this.resource = resource;
		this.parentScope = parentScope;
		this.prototype = NativeObject.getObjectPrototype(parentScope);
	}

	@Override
	public String getClassName() {
		return getClass().getSimpleName();
	}

	@Override
	public Object get(String name, Scriptable start) {
		return resource.containsKey(name) ? resource.message(name) : Scriptable.NOT_FOUND;
	}

	@Override
	public Object get(int index, Scriptable start) {
		return get(String.valueOf(index), start);
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return resource.containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return has(String.valueOf(index), start);
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		// ignored
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		// ignored
	}

	@Override
	public void delete(String name) {
		// ignored
	}

	@Override
	public void delete(int index) {
		// ignored
	}

	@Override
	public Scriptable getPrototype() {
		return prototype;
	}

	@Override
	public void setPrototype(Scriptable prototype) {
		this.prototype = prototype;
	}

	@Override
	public Scriptable getParentScope() {
		return parentScope;
	}

	@Override
	public void setParentScope(Scriptable parent) {
		parentScope = parent;
	}

	@Override
	public Object[] getIds() {
		Set<String> keys = resource.keys();
		return keys.toArray(new Object[keys.size()]);
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		
		if (hint == null) {
			return this;
		}
		if (hint == String.class) {
			return getClassName();
		}
		if (hint == Number.class) {
			return 0;
		}
		if (hint == Boolean.class) {
			return true;
		}
		return Undefined.instance;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		return instance instanceof MessagesScriptable;
	}
	
}