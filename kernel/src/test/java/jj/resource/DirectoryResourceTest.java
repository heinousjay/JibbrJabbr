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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;

import jj.Base;
import jj.configuration.resolution.AppLocation;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class DirectoryResourceTest {

	@Test
	public void test() throws IOException {
		
		Location base = AppLocation.Base;
		String name = "helpers";
		Path path = Base.appPath().resolve(name);
		
		DirectoryResource r = new DirectoryResource(new MockAbstractResourceDependencies(base, name), path);
		
		assertThat(r, is(notNullValue()));

		name = "internal";
		path = Base.appPath().resolve(name);
		
		r = new DirectoryResource(new MockAbstractResourceDependencies(base, name), path);
		
		assertThat(r, is(notNullValue()));

		name = "nope";
		path = Base.appPath().resolve(name);
		
		try {
			new DirectoryResource(new MockAbstractResourceDependencies(base, name), path);
			fail();
		} catch (NoSuchResourceException nsre) {}

		name = "blank.gif";
		path = Base.appPath().resolve(name);
		
		try {
			new DirectoryResource(new MockAbstractResourceDependencies(base, name), path);
			fail();
		} catch (NoSuchResourceException nsre) {}
		
	}

}
