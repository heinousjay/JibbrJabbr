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
import static org.mockito.BDDMockito.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.SHA1Helper;
import jj.configuration.Configuration;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class ResourceBase {
	
	URI baseUri;
	Path basePath;
	@Mock Configuration configuration;
	
	@Before
	public final void setup() throws Exception {
		baseUri = URI.create("http://localhost:8080/");
		basePath = Paths.get(ResourceBase.class.getResource("/index.html").toURI()).getParent();
		given(configuration.basePath()).willReturn(basePath);
		given(configuration.baseUri()).willReturn(baseUri);
	}
		

	protected <T extends Resource> T testFileResource(
		final String baseName,
		final ResourceCreator<T> toTest,
		Object...args
	) throws Exception {
		final Path path = toTest.toPath(baseName, args);
		
		assertTrue(baseName + " does not exist", Files.exists(path));
		
		T resource1 = toTest.create(baseName, args);
		final byte[] bytes = Files.readAllBytes(path);
		assertThat(resource1, is(notNullValue()));
		assertThat(resource1.baseName(), is(baseName));
		assertThat(resource1.lastModified(), is(Files.getLastModifiedTime(path)));
		assertThat(resource1.path(), is(path));
		assertThat(resource1.needsReplacing(), is(false));
		
		if (!(resource1 instanceof CssResource)) { 
			assertThat(resource1.sha1(), is(SHA1Helper.keyFor(bytes))); 
		}
		
		return resource1;
	}
}
