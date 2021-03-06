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
package jj;

import java.util.HashMap;
import java.util.Map;

import jj.util.Closer;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * <p>
 * Inject this to scope some object creation.
 * 
 * @author jason
 *
 */
public class CreationScope implements Scope {
	
	/**
	 * Returns a provider that always throws exception complaining that the
	 * object in question must be seeded before it can be injected.
	 *
	 * @return typed provider
	 */
	@SuppressWarnings("unchecked")
	public static <T> Provider<T> seededKeyProvider() {
		return (Provider<T>)SEEDED_KEY_PROVIDER;
	}
	
	private static final Provider<Object> SEEDED_KEY_PROVIDER =
			() -> {
				throw new AssertionError(
					"scoped object should have been " +
					"explicitly seeded in this scope by calling " +
					"#seed(), but was not."
				);
			};

	CreationScope() {}
	
	private final ThreadLocal<Map<Key<?>, Object>> values = new ThreadLocal<>();

	@Override
	public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
		return () -> {
			Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

			@SuppressWarnings("unchecked")
			T current = (T)scopedObjects.get(key);
			if (current == null && !scopedObjects.containsKey(key)) {
				current = unscoped.get();
				scopedObjects.put(key, current);
			}
			return current;
		};
	}

	public Closer enter() {
		assert(values.get() == null) : "A scoping block is already in progress";
		values.set(new HashMap<>());
		return values::remove;
	}

	public <T> CreationScope seed(Key<T> key, T value) {
		Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);
		if (!scopedObjects.containsKey(key)) {
			throw new AssertionError(String.format(
				"A value for the key %s was " + "already seeded in this scope. Old value: %s New value: %s",
				key,
				scopedObjects.get(key),
				value
			));
		}
		scopedObjects.put(key, value);
		return this;
	}

	public <T> CreationScope seed(Class<T> clazz, T value) {
		return seed(Key.get(clazz), value);
	}

	private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
		Map<Key<?>, Object> scopedObjects = values.get();
		if (scopedObjects == null) {
			throw new AssertionError("Cannot access " + key + " outside of a scoping block");
		}
		return scopedObjects;
	}
	
	@Override
	public String toString() {
		return "JibbrJabbr.CREATION";
	}
}
