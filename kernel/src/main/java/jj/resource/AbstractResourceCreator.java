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

import java.net.URI;

import jj.configuration.AppLocation;

/**
 * @author jason
 *
 */
public abstract class AbstractResourceCreator<T extends AbstractResource> implements ResourceCreator<T> {

	protected abstract URI uri(final AppLocation base, final String name, final Object...args);
	
	@Override
	public ResourceCacheKey cacheKey(final AppLocation base, final String name, final Object...args) {
		return new ResourceCacheKey(type(), base, uri(base, name, args));
	}
	
	ResourceCacheKey cacheKey(URI uri) {
		// app location doesn't matter here?  not sure yet
		// in fact i think it does and i think it's going to get passed in from the user, which is... 
		return new ResourceCacheKey(type(), AppLocation.Base, uri);
	}
}
