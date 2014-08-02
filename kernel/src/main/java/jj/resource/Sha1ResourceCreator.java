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
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
class Sha1ResourceCreator extends SimpleResourceCreator<Sha1Resource> {
	
	static final String EXTENSION = ".sha1";
	
	// a wrapper to keep guice happy
	static class Sha1ResourceTarget {
		
		final AbstractFileResource resource;
		
		Sha1ResourceTarget(final AbstractFileResource resource) {
			this.resource = resource;
		}
	}

	@Inject
	Sha1ResourceCreator(final Dependencies dependencies) {
		super(dependencies);
	}
	
	@Override
	public Sha1Resource create(Location base, String name, Object... args) throws IOException {
		assert args.length == 1 && args[0] instanceof AbstractFileResource : "Sha1Resources can only be created in relation to an AbstractFileResource";
		
		AbstractFileResource resource = (AbstractFileResource)args[0];
		Path path = Paths.get(resource.path.toString().concat(EXTENSION));
		
		return creator.createResource(type(), resourceKey(base, name), base, name, path, new Sha1ResourceTarget(resource));
	}
}
