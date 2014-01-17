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
package jj.resource.config;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
@Singleton
public class ConfigResourceCreator extends AbstractResourceCreator<ConfigResource> {
	
	private final Configuration configuration;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	ConfigResourceCreator(
		final Configuration configuration,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.configuration = configuration;
		this.instanceModuleCreator = instanceModuleCreator;
	}

	@Override
	public Class<ConfigResource> type() {
		return ConfigResource.class;
	}

	@Override
	public boolean canLoad(String name, Object... args) {
		return ConfigResource.CONFIG_JS.equals(name);
	}
	
	@Override
	protected URI uri(String baseName, Object... args) {
		return path(baseName).toUri();
	}
	
	private Path path(String baseName, Object... args) {
		return canLoad(baseName) ? configuration.appPath().resolve(baseName) : null;
	}

	@Override
	public ConfigResource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(ConfigResource.class, cacheKey(baseName), baseName, path(baseName));
	}

}
