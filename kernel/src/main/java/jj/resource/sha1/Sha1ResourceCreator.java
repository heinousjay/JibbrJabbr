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
package jj.resource.sha1;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
@Singleton
public class Sha1ResourceCreator extends AbstractResourceCreator<Sha1Resource> {

	private final Arguments arguments;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	Sha1ResourceCreator(
		final Arguments arguments,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.arguments = arguments;
		this.instanceModuleCreator = instanceModuleCreator;
	}

	@Override
	public Class<Sha1Resource> type() {
		return Sha1Resource.class;
	}

	@Override
	public boolean canLoad(String name, Object... args) {
		return name.endsWith("sha1");
	}

	@Override
	public Sha1Resource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(Sha1Resource.class, cacheKey(baseName), baseName, path(baseName));
	}
	
	@Override
	protected URI uri(String baseName, Object... args) {
		return path(baseName).toUri();
	}

	private Path path(String baseName, Object... args) {
		return arguments.appPath().resolve(baseName);
	}

}
