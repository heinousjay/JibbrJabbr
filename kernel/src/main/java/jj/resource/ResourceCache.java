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
import java.util.List;

/**
 * @author jason
 *
 */
interface ResourceCache {

	/**
	 * @param uri
	 * @return
	 */
	List<AbstractResource> findAllByUri(URI uri);

	/**
	 * @param cacheKey
	 * @return
	 */
	AbstractResource get(ResourceKey cacheKey);
	
	AbstractResource putIfAbsent(ResourceKey cacheKey, AbstractResource resource);
	
	<T extends Resource> ResourceCreator<T> getCreator(final Class<T> type);

	/**
	 * @param cacheKey
	 * @param resource
	 * @return
	 */
	boolean remove(ResourceKey cacheKey, AbstractResource resource);
	
	boolean replace(ResourceKey key, AbstractResource oldValue, AbstractResource newValue);

}