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
package jj.util;

import jj.execution.CurrentTask;

/**
 * <p>
 * Don't forget! Descendents of this class should be annotated with 
 * {@link javax.inject.Singleton} or there really is just no point
 * 
 * <p>
 * Performs the basic management of a contextual resource with a generic closer. 
 * <pre class="brush:java">
 * try (Closer closer = resource.enterScope(..something..)) {
 *     // use that resource
 * }
 * </pre>
 * 
 * <p>
 * If initialization or cleanup of the resource is needed, see {@link CurrentResourceAware}.
 * 
 * <p>
 * Subclasses of this classes act as system-wide thread-scoped context locators for resources,
 * and can be injected and queried to perform work with those resources.  For instance, the
 * currently executing task can be obtained by calling {@link CurrentTask#current()}.
 * 
 * @author jason
 *
 */
public abstract class CurrentResource<RESOURCE> {
	
	protected final ThreadLocal<RESOURCE> resources = new ThreadLocal<>();

	public final Closer enterScope(final RESOURCE resource) {
		final String name = getClass().getSimpleName();
		
		assert resources.get() == null : "attempting to nest in " + name;
		
		resources.set(resource);
		
		if (resource instanceof CurrentResourceAware) {
			((CurrentResourceAware)resource).enteredCurrentScope();
		}
		
		return new Closer() {
			
			@Override
			public void close() {
				resources.set(null);
				
				if (resource instanceof CurrentResourceAware) {
					((CurrentResourceAware)resource).exitedCurrentScope();
				}
			}
		};
	}
	
	public RESOURCE current() {
		return resources.get();
	}
}
