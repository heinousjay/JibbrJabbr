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
package jj.messaging;

import java.io.IOException;
import java.util.Locale;

import jj.resource.AbstractResource;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;

/**
 * <p>
 * Represents a collection of {@link PropertiesResource}s that are accessed in
 * a manner similar to the {@link java.util.PropertyResourceBundle}.
 * 
 * <p>
 * Identify by a name and a {@link Locale}
 * 
 * @author jason
 *
 */
public class MessagesResource extends AbstractResource {
	
	private final String name;
	private final Locale locale;

	MessagesResource(
		final ResourceCacheKey cacheKey,
		final String name,
		final Locale locale,
		final ResourceFinder resourceFinder
	) {
		super(cacheKey);
		
		this.name = name;
		this.locale = locale;
	}
	
	public Locale locale() {
		return locale;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String uri() {
		return "/" + name;
	}

	@Override
	public String sha1() {
		return null;
	}

	@Override
	public boolean needsReplacing() throws IOException {
		// always replaced by dependencies
		return false;
	}

}
