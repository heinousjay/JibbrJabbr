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

import java.nio.file.Path;

/**
 * @author jason
 *
 */
public class PropertiesResourceCreatorTest extends ResourceBase<PropertiesResource, PropertiesResourceCreator> {

	@Override
	protected String baseName() {
		return "index";
	}

	@Override
	protected Path path() {
		return basePath.resolve(baseName() + ".properties");
	}

	@Override
	protected PropertiesResource resource() throws Exception {
		return new PropertiesResource(cacheKey(), path(), baseName());
	}

	@Override
	protected PropertiesResourceCreator toTest() {
		return new PropertiesResourceCreator(configuration, instanceModuleCreator);
	}


}
