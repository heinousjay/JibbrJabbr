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
package jj.resource;

import java.util.Arrays;

/**
 * the root of the hierarchy of resource events
 * @author jason
 *
 */
public class ResourceEvent {
	
	public final Class<? extends Resource> resourceClass;
	public final String name;
	public final Object[] arguments;
	
	protected ResourceEvent(final Class<? extends Resource> resourceClass, final String name, final Object...arguments) {
		this.resourceClass = resourceClass;
		this.name = name;
		this.arguments = arguments;
	}
	
	public boolean matches(final Class<? extends Resource> resourceClass, final String name, final Object...arguments) {
		return this.resourceClass == resourceClass &&
			this.name.equals(name) &&
			Arrays.equals(this.arguments, arguments);
	}

}
