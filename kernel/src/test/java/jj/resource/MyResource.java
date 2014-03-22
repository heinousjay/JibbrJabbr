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

import jj.configuration.AppLocation;

class MyResource extends AbstractResource {

	private final URI uri;
	
	protected MyResource(URI uri) {
		super(new ResourceCacheKey(MyResource.class, uri), AppLocation.Base);
		this.uri = uri;
	}

	@Override
	public String name() {
		return uri.toString();
	}

	@Override
	public String uri() {
		return uri.toString();
	}

	@Override
	public String sha1() {
		// this is not important for this test
		return "";
	}

	@Override
	public boolean needsReplacing() throws IOException {
		return false;
	}
}