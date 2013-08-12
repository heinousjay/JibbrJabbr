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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class ConfigResource extends AbstractFileResource {

	public static final String CONFIG_JS = "config.js";
	
	/**
	 * @param baseName
	 * @param path
	 * @throws IOException
	 */
	@Inject
	ConfigResource(final ResourceCacheKey cacheKey, final Path path) throws IOException {
		super(cacheKey, CONFIG_JS, path);
	}

	@Override
	public String uri() {
		// no serving this!
		return "/";
	}

	@Override
	public String mime() {
		return MimeTypes.getDefault();
	}
	
	public String script() {
		return byteBuffer.toString(UTF_8);
	}
}
