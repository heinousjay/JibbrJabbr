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
package jj.script.module;

import java.nio.file.Path;

import jj.configuration.resolution.AppLocation;
import jj.resource.AbstractResource.Dependencies;
import jj.resource.ResourceBase;
import jj.script.module.ScriptResource;
import jj.script.module.ScriptResourceCreator;
import jj.script.module.ScriptResourceType;

/**
 * @author jason
 *
 */
public class ScriptResourceCreatorTest extends ResourceBase<ScriptResource, ScriptResourceCreator> {

	@Override
	protected String name() {
		return ScriptResourceType.Client.suffix("index");
	}

	protected Path path() {
		return appPath.resolve(name());
	}

	@Override
	protected ScriptResource resource() throws Exception {
		return new ScriptResource(new Dependencies(cacheKey(), AppLocation.Base), path(), name());
	}

	@Override
	protected ScriptResourceCreator toTest() {
		return new ScriptResourceCreator(app, creator);
	}

}
