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
package jj.resource.stat.ic;

import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.configuration.Application;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
@Singleton
public class StaticResourceCreator extends AbstractResourceCreator<StaticResource> {

	private final Application app;
	private final ResourceInstanceCreator instanceModuleCreator;

	@Inject
	StaticResourceCreator(
		final Application app,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.app = app;
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	public Class<StaticResource> type() {
		return StaticResource.class;
	}
	
	@Override
	public boolean canLoad(String name, Object... args) {
		return true;
	}
	
	@Override
	protected URI uri(AppLocation base, String name, Object... args) {
		return app.resolvePath(base, name).toUri();
	}

	@Override
	public StaticResource create(AppLocation base, String name, Object... args) throws IOException {
		return instanceModuleCreator.createResource(StaticResource.class, cacheKey(base, name), base, name);
	}

}
