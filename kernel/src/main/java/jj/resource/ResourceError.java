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
public class ResourceError extends ResourceEvent {

	private final Throwable t;
	
	<T extends Resource<A>, A> ResourceError(ResourceIdentifier<T, A> resourceIdentifier, Throwable t) {
		super(resourceIdentifier);
		this.t = t;
	}

	@Override
	protected String description() {
		return "error loading resource";
	}
	
	@Override
	public void describeTo(Logger logger) {
		logger.error("{} - {}", description(), identifier, t);
	}

}
