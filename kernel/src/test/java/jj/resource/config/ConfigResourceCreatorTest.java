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

import static jj.resource.config.ConfigResource.CONFIG_JS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;

import jj.resource.ResourceBase;
import jj.resource.config.ConfigResource;
import jj.resource.config.ConfigResourceCreator;
import jj.script.RealRhinoContextProvider;

/**
 * @author jason
 *
 */
public class ConfigResourceCreatorTest extends ResourceBase<ConfigResource, ConfigResourceCreator> {

	@Override
	protected String name() {
		return CONFIG_JS;
	}
	
	protected Path path() {
		return appPath.resolve(name());
	}
	
	@Override
	protected ConfigResource resource() throws Exception {
		return new ConfigResource(new RealRhinoContextProvider(), cacheKey(), path());
	}
	
	@Override
	protected void resourceAssertions(ConfigResource resource) throws Exception {
		assertThat(resource.functions().keySet(), containsInAnyOrder("fails", "http", "scriptTestInterface", "httpServerSocket", "document"));
	}
	
	@Override
	protected ConfigResourceCreator toTest() {
		return new ConfigResourceCreator(app, creator);
	}
}
