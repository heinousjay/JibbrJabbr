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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import jj.Base;
import jj.event.Publisher;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class RealResourceBase {
	
	protected Path appPath;
	@Mock protected Publisher publisher;

	@Before
	public final void init() throws Exception {
		appPath = Base.appPath();
	}
	
	protected <T extends Resource> T testResource(final T resource) throws Exception {
		
		if (resource instanceof FileResource) {
			testFileResource((FileResource)resource);
		} else {
			testNonFileResource(resource);
		}
		
		return resource;
	}
	
	private <T extends FileResource> T testFileResource(final T resource) throws Exception {
		// well this is weirdly ugly haha
		final Path path = resource.path();
		
		assertTrue(resource.name() + " does not exist", Files.exists(path));
		
		testNonFileResource(resource);

		return resource;
	}

	private <T extends Resource> T testNonFileResource(final T resource) throws Exception {
		
		assertThat(resource, is(instanceOf(AbstractResource.class)));
		 
		assertThat(resource, is(notNullValue()));
		assertThat(resource.name(), is(notNullValue()));
		assertThat(resource.name(), not(Matchers.startsWith("/")));
		assertThat(((AbstractResource)resource).needsReplacing(), is(false));
		
		return resource;
	}

}
