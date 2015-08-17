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

/**
 * @author jason
 *
 */
public class ResourceNotFound extends ResourceEvent {
	
	public <A, T extends Resource<A>> ResourceNotFound(final Class<T> resourceClass, final Location base, final String name, final A argument) {
		super(resourceClass, base, name, argument);
	}
	
	@Override
	protected String description() {
		return "resource not found";
	}
	
	@Override
	public void describeTo(Logger logger) {
		logger.trace("{} - {} at {}", description(), resourceClass, name);
	}
}
