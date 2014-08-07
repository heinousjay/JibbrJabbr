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

import jj.configuration.resolution.AppLocation;
import jj.event.Publisher;
import jj.util.SHA1Helper;

class MyResource extends AbstractResource {

	private final URI uri;
	
	protected MyResource(String name, Publisher publisher) {
		super(new MockAbstractResourceDependencies(new ResourceKey(MyResource.class, URI.create(name)), AppLocation.Base, name, publisher));
		this.uri = URI.create(name);
	}

	@Override
	public String uri() {
		return uri.toString();
	}

	@Override
	public String sha1() {
		return SHA1Helper.keyFor(name);
	}

	@Override
	public boolean needsReplacing() throws IOException {
		return false;
	}
}