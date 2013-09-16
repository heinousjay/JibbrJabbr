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
package jj.resource.spec;

import java.io.IOException;
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
public class SpecResourceCreator extends AbstractResourceCreator<SpecResource> {

	private final Configuration configuration;
	private final ResourceInstanceCreator creator;
	
	@Inject
	SpecResourceCreator(
		final Configuration configuration,
		final ResourceInstanceCreator creator
	) {
		this.configuration = configuration;
		this.creator = creator;
	}

	@Override
	public Class<SpecResource> type() {
		return SpecResource.class;
	}

	@Override
	public boolean canLoad(String name, Object... args) {
		return name.endsWith(".js");
	}

	@Override
	public SpecResource create(String baseName, Object... args) throws IOException {
		return creator.createResource(
			SpecResource.class, 
			cacheKey(baseName),
			baseName,
			path(baseName)
		);
	}

	@Override
	protected Path path(String baseName, Object... args) {
		return basePath().resolve(baseName);
	}
	
	private Path basePath() {
		return configuration.appPath().resolveSibling("specs");
	}

}
