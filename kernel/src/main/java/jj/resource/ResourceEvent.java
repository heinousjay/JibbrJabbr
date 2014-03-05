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

import org.slf4j.Logger;

import jj.configuration.AppLocation;
import jj.logging.LoggedEvent;

/**
 * the root of the hierarchy of resource events
 * @author jason
 *
 */
@ResourceLogger
public abstract class ResourceEvent implements LoggedEvent {
	
	public final Class<? extends Resource> resourceClass;
	public final AppLocation base;
	public final String name;
	public final Object[] arguments;
	
	protected ResourceEvent(final Class<? extends Resource> resourceClass, final AppLocation base, final String name, final Object...arguments) {
		this.resourceClass = resourceClass;
		this.base = base;
		this.name = name;
		this.arguments = arguments;
	}
	
	public boolean matches(final Class<? extends Resource> resourceClass, final AppLocation base, final String name, final Object...arguments) {
		return this.resourceClass == resourceClass &&
			this.base == base &&
			this.name.equals(name) &&
			Arrays.equals(this.arguments, arguments);
	}
	
	public boolean matches(final AbstractResource resource) {
		return matches(resource.getClass(), resource.cacheKey().base(), resource.name(), resource.creationArgs());
	}
	
	protected abstract String description();
	
	@Override
	public void describeTo(Logger log) {
		log.info("{} - {} at {}/{}", description(), resourceClass, base, name);
	}

}
