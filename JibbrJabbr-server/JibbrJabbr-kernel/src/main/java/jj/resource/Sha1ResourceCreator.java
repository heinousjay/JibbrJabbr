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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;

/**
 * @author jason
 *
 */
@Singleton
class Sha1ResourceCreator extends AbstractResourceCreator<Sha1Resource> {

	private final Configuration configuration;
	private final ResourceInstanceModuleCreator instanceModuleCreator;
	
	@Inject
	Sha1ResourceCreator(
		final Configuration configuration,
		final ResourceInstanceModuleCreator instanceModuleCreator
	) {
		this.configuration = configuration;
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
	Path path(String baseName, Object... args) {
		return configuration.basePath().resolve(baseName);
	}

}