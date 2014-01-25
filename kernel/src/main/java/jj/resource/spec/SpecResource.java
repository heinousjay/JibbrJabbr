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
package jj.resource.spec;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Path;

import javax.inject.Inject;

import jj.resource.AbstractFileResource;
import jj.resource.MimeTypes;
import jj.resource.ResourceCacheKey;

/**
 * @author jason
 *
 */
public class SpecResource extends AbstractFileResource {

	private final String script;
	
	@Inject
	SpecResource(final ResourceCacheKey cacheKey, final String baseName, final Path path) {
		super(cacheKey, baseName, path);
		script = byteBuffer.toString(UTF_8);
	}

	@Override
	public String mime() {
		return MimeTypes.get(".js");
	}
	
	public String script() {
		return script;
	}
}