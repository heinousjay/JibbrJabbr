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

/**
 * Performs the basic management of a resource with a generic closer. just a wrapper around
 * a threadlocal to give it that nice syntax
 * <pre>
 * try (Closer closer = resource.enterScope(..something..)) {
 *     // use that resource
 * }
 * </pre>
 * @author jason
 *
 */
public abstract class CurrentResource<RESOURCE> {
	
	protected final ThreadLocal<RESOURCE> resources = new ThreadLocal<>();

	public final Closer enterScope(final RESOURCE resource) {
		final String name = getClass().getSimpleName();
		
		assert resources.get() == null : "attempting to nest in " + name;
		
		resources.set(resource);
		
		if (resource instanceof ResourceAware) {
			((ResourceAware)resource).start();
		}
		
		return new Closer() {
			
			@Override
			public void close() {
				resources.set(null);
				
				if (resource instanceof ResourceAware) {
					((ResourceAware)resource).end();
				}
			}
		};
	}
	
	public RESOURCE current() {
		return resources.get();
	}
}