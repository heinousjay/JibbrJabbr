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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author jason
 *
 */
public class StaticResource extends AbstractFileResource {

	private final String mime;
	private final String absoluteUri;
	
	/**
	 * @param baseName
	 * @param path
	 * @throws IOException
	 */
	StaticResource(final URI baseUri, final Path basePath, final String baseName) throws IOException {
		super(baseName, basePath.resolve(baseName));
		mime = MimeTypes.get(baseName);
		absoluteUri = baseUri.toString() + baseName;
	}

	@Override
	public String uri() {
		return "/" + baseName;
	}

	@Override
	public String absoluteUri() {
		return absoluteUri;
	}

	@Override
	public String mime() {
		return mime;
	}
	
	@Override
	public boolean cache() {
		return false;
	}
	
	// this is bad!
	public byte[] bytes() {
		return bytes;
	}

}
