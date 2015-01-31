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

import jj.util.Closer;
import jj.util.CurrentResourceAware;

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
 * Don't forget! Descendents of this class should be annotated with 
 * {@link javax.inject.Singleton} or there really is just no point
 * 
 * <p>
 * If initialization or cleanup of the resource is needed, see {@link CurrentResourceAware}.
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
	
	protected final ThreadLocal<T> carrier = new ThreadLocal<>();

	/**
	 * Begin exposing 
	 * @param instance
	 * @return
	 */
	public final Closer enterScope(final T instance) {
		final String name = getClass().getSimpleName();
		
		assert carrier.get() == null : "attempting to nest in " + name;
		
		carrier.set(instance);
		
		if (instance instanceof CurrentResourceAware) {
			((CurrentResourceAware)instance).enteredCurrentScope();
		}
		
		return new Closer() {
			
			@Override
			public void close() {
				carrier.set(null);
				
				if (instance instanceof CurrentResourceAware) {
					((CurrentResourceAware)instance).exitedCurrentScope();
				}
			}
		};
	}
	
	public T current() {
		return carrier.get();
	}
}
