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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;

/**
 * @author jason
 *
 */
@Singleton
class StaticResourceCreator implements ResourceCreator<StaticResource> {

	private final URI baseUri;
	private final Path basePath;

	@Inject
	StaticResourceCreator(final Configuration configuration) {
		this.baseUri = configuration.baseUri();
		this.basePath = configuration.basePath();
	}
	
	@Override
	public Class<StaticResource> type() {
		return StaticResource.class;
	}

	@Override
	public Path toPath(String baseName, Object... args) {
		return basePath.resolve(baseName);
	}

	@Override
	public StaticResource create(String baseName, Object... args) throws IOException {
		StaticResource s = new StaticResource(baseUri, basePath, baseName);
		return s;
	}

}
