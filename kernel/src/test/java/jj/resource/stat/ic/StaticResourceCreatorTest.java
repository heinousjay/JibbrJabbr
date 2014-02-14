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

import java.nio.file.Path;

import jj.resource.ResourceBase;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceCreator;

/**
 * @author jason
 *
 */
public class StaticResourceCreatorTest extends ResourceBase<StaticResource, StaticResourceCreator> {

	@Override
	protected String baseName() {
		return "helpers/jquery.fancybox-media.js";
	}

	protected Path path() {
		return appPath.resolve(baseName());
	}

	@Override
	protected StaticResource resource() throws Exception {
		return new StaticResource(cacheKey(), path(), baseName());
	}

	@Override
	protected StaticResourceCreator toTest() {
		return new StaticResourceCreator(arguments, creator);
	}

}
