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
import java.nio.file.Path;

/**
 * @author jason
 *
 */
public abstract class AbstractResourceCreator<T extends AbstractResource> implements ResourceCreator<T> {

	protected abstract Path path(final String baseName, final Object...args);
	
	@Override
	public final ResourceCacheKey cacheKey(String baseName, Object...args) {
		return new ResourceCacheKey(type(), path(baseName, args).toUri());
	}
	
	@Override
	public final ResourceCacheKey cacheKey(URI uri) {
		return new ResourceCacheKey(type(), uri);
	}
}
