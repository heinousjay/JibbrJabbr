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

import jj.configuration.Configuration;

/**
 * @author jason
 *
 */
class ConfigResourceCreator extends AbstractResourceCreator<ConfigResource> {
	
	private final Configuration configuration;
	
	ConfigResourceCreator(final Configuration configuration) {
		this.configuration = configuration;
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
	Path path(String baseName, Object... args) {
		return canLoad(baseName) ? configuration.basePath().resolve(baseName) : null;
	}

	@Override
	public ConfigResource create(String baseName, Object... args) throws IOException {
		return new ConfigResource(cacheKey(baseName), path(baseName));
	}

}
