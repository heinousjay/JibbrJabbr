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
package jj.resource.property;

import java.nio.file.Path;

import jj.resource.ResourceBase;
import jj.resource.property.PropertiesResource;
import jj.resource.property.PropertiesResourceCreator;

/**
 * @author jason
 *
 */
public class PropertiesResourceCreatorTest extends ResourceBase<PropertiesResource, PropertiesResourceCreator> {

	@Override
	protected String name() {
		return "index";
	}

	protected Path path() {
		return appPath.resolve(name() + ".properties");
	}

	@Override
	protected PropertiesResource resource() throws Exception {
		return new PropertiesResource(cacheKey(), path(), name());
	}

	@Override
	protected PropertiesResourceCreator toTest() {
		return new PropertiesResourceCreator(app, creator);
	}


}
