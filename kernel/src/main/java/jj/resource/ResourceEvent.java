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

import org.slf4j.Logger;

import jj.logging.LoggedEvent;

/**
 * the root of the hierarchy of resource events
 * @author jason
 *
 */
@ResourceLogger
public abstract class ResourceEvent extends LoggedEvent {

	protected final ResourceIdentifier<? ,?> identifier;
	
	protected ResourceEvent(final ResourceIdentifier<? ,?> identifier) {
		assert identifier != null;
		this.identifier = identifier;
	}
	
	public <T extends Resource<A>, A> boolean matches(Class<T> resourceClass, Location base, String name, A argument) {
		return identifier.equals(resourceClass, base, name, argument);
	}

	public boolean matches(ResourceIdentifier<?, ?> other) {
		return other != null && other.equals(identifier);
	}

	public boolean matches(Resource<?> resource) {
		return matches(resource.identifier());
	}

	@SuppressWarnings("unchecked")
	public <T extends Resource<A>, A> ResourceIdentifier<T, A> identifier() {
		return (ResourceIdentifier<T, A>)identifier;
	}

	public Class<?> type() {
		return identifier.resourceClass;
	}

	public Location base() {
		return identifier.base;
	}

	public String name() {
		return identifier.name;
	}

	public Object argument() {
		return identifier.argument;
	}
	
	protected abstract String description();
	
	@Override
	public void describeTo(Logger log) {
		log.info("{} - {}", description(), identifier);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + identifier + ")";
	}

}
