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
package jj.jasmine;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;

import jj.configuration.resolution.AppLocation;
import jj.jasmine.SpecResource;
import jj.jasmine.SpecResourceCreator;
import jj.resource.AbstractResource.Dependencies;
import jj.resource.ResourceBase;

/**
 * @author jason
 *
 */
public class SpecResourceCreatorTest extends ResourceBase<SpecResource, SpecResourceCreator>{

	@Override
	protected String name() {
		return "its_a_spec.js";
	}

	protected Path path() {
		return appPath.resolveSibling("specs").resolve(name());
	}

	@Override
	protected SpecResource resource() throws Exception {
		return new SpecResource(new Dependencies(cacheKey(), AppLocation.Base), name(), path());
	}

	@Override
	protected SpecResourceCreator toTest() {
		return new SpecResourceCreator(app, creator);
	}

	@Override
	protected void resourceAssertions(SpecResource resource) throws Exception {
		assertThat(resource.script().getBytes(UTF_8),  is(Files.readAllBytes(path())));
	}
}
