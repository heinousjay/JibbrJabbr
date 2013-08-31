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

import static jj.resource.ConfigResource.CONFIG_JS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;

import jj.script.RealRhinoContextMaker;

/**
 * @author jason
 *
 */
public class ConfigResourceCreatorTest extends ResourceBase<ConfigResource, ConfigResourceCreator> {

	@Override
	protected String baseName() {
		return CONFIG_JS;
	}
	
	@Override
	protected Path path() {
		return appPath.resolve(baseName());
	}
	
	@Override
	protected ConfigResource resource() throws Exception {
		return new ConfigResource(new RealRhinoContextMaker(), cacheKey(), path());
	}
	
	@Override
	protected void resourceAssertions(ConfigResource resource) throws Exception {
		assertThat(resource.functions().keySet(), containsInAnyOrder("fails", "http", "scriptTestInterface", "httpServerSocket", "document"));
	}
	
	@Override
	protected ConfigResourceCreator toTest() {
		return new ConfigResourceCreator(configuration, instanceModuleCreator);
	}
}
