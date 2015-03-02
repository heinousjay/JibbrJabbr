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
package jj.execution;

import javax.inject.Inject;

import jj.util.Closer;

/**
 * <p>
 * Descend from this to the basic management of a contextual resource that
 * can be used in a try-with-resources
 * <pre class="brush:java">
 * try (Closer closer = resource.enterScope(..something..)) {
 *     // use that resource (presumably in a disconnected part of the system)
 *     resource.current()...;
 * }
 * </pre>
 * 
 * <p>
 * Don't forget! Descendants of this class should be annotated with 
 * {@link javax.inject.Singleton} or there really is just no point
 * 
 * <p>
 * If initialization or cleanup of the resource is needed, see {@link ExecutionLifecycleAware}.
 * 
 * <p>
 * Subclasses of this classes act as system-wide thread-scoped context locators for resources,
 * and can be injected and queried to perform work with those resources.  For instance, the
 * currently executing task can be obtained by calling {@link CurrentTask#current()}.
 * 
 * @param <T> The type of resource being managed
 * 
 * @author jason
 *
 */
public abstract class ExecutionInstance<T> {
	
	// this field is initialized for unit tests,
	// but gets replaced with the appropriate
	// instance in the running system
	@Inject private ExecutionInstanceStorage storage = new ExecutionInstanceStorage();

	/**
	 * Begin exposing 
	 * @param instance
	 * @return
	 */
	public final Closer enterScope(final T instance) {
		assert storage.get(getClass()) == null;
		storage.set(getClass(), instance);
		if (instance instanceof ExecutionLifecycleAware) {
			((ExecutionLifecycleAware)instance).enteredScope();
		}
		
		return new Closer() {
			
			@Override
			public void close() {
				storage.clear(ExecutionInstance.this.getClass());
				
				if (instance instanceof ExecutionLifecycleAware) {
					((ExecutionLifecycleAware)instance).exitedScope();
				}
			}
		};
	}
	
	public T current() {
		return storage.get(getClass());
	}
}
