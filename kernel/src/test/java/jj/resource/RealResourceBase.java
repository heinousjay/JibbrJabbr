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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.file.Files;
import java.nio.file.Path;

import jj.BasePath;
import jj.SHA1Helper;
import jj.configuration.Configuration;
import jj.resource.css.CssResource;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class RealResourceBase {
	
	protected Path appPath;
	@Mock protected Configuration configuration;
	@Mock protected Logger logger;

	@Before
	public final void init() throws Exception {
		appPath = BasePath.appPath();
		given(configuration.appPath()).willReturn(appPath);
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
		
		assertTrue(resource.baseName() + " does not exist", Files.exists(path));
		
		testNonFileResource(resource);
		
		if (!(resource instanceof CssResource)) { 
			final byte[] bytes = Files.readAllBytes(path);
			assertThat(resource.sha1(), is(SHA1Helper.keyFor(bytes))); 
		}
		return resource;
	}

	private <T extends Resource> T testNonFileResource(final T resource) throws Exception {
		
		assertThat(resource, is(instanceOf(AbstractResource.class)));
		 
		assertThat(resource, is(notNullValue()));
		assertThat(resource.baseName(), is(notNullValue()));
		assertThat(resource.baseName(), not(Matchers.startsWith("/")));
		assertThat(((AbstractResource)resource).needsReplacing(), is(false));
		
		if (!(resource instanceof CssResource) && (resource instanceof LoadedResource)) { 

			ByteBuf buf = ((LoadedResource)resource).bytes();
			buf.readBytes(Unpooled.buffer(buf.readableBytes()));
			// this test is to ensure that all buffers are wrapped before returning
			// since this has caught me out twice now
			assertThat(((LoadedResource)resource).bytes().readableBytes(), greaterThan(0));
		}
		
		return resource;
	}

}
