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

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class DirectoryResourceCreator extends AbstractResourceCreator<DirectoryResource> {
	
	private final PathResolver pathResolver;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	DirectoryResourceCreator(
		final PathResolver pathResolver,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.pathResolver = pathResolver;
		this.instanceModuleCreator = instanceModuleCreator;
	}

	@Override
	protected URI uri(Location base, String name, Object... args) {
		assert base.parentInDirectory() : "wrong mode for " + base;
		return pathResolver.resolvePath(base, name).toUri();
	}

	@Override
	public DirectoryResource create(Location base, String name, Object... args) throws IOException {
		assert base.parentInDirectory() : "wrong mode for " + base;
		return instanceModuleCreator.createResource(DirectoryResource.class, resourceKey(base, name), base, name);
	}

}
