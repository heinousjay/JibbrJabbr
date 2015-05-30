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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static jj.application.AppLocation.AppBase;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.util.SHA1Helper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * it's a mighty concrete test but it does use the file system
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Sha1ResourceTest {
	
	@Mock AbstractFileResource afs;
	@Mock ResourceKey resourceKey;
	MockAbstractResourceDependencies dependencies;
	
	final String indexHtml = "index.html";
	final String indexSha1 = indexHtml + ".sha1";
	
	Path index;
	Path iSha1;
	String shaKey;
	long size;
	
	private Path pathFor(String resourcePath) throws Exception {
		URL url = Sha1ResourceTest.class.getResource(resourcePath);
		return url == null ? null : Paths.get(url.toURI());
	}
	
	@Before
	public void before() throws Exception {
		dependencies = new MockAbstractResourceDependencies(resourceKey, AppBase, indexHtml);
		
		Path path = pathFor(indexSha1);
		if (path != null && Files.exists(path)) {
			Files.delete(path);
		}
		
		index = pathFor(indexHtml);
		iSha1 = index.resolveSibling(indexSha1);
		shaKey = SHA1Helper.keyFor(index);
		size = (long)Files.getAttribute(index, "size");

		given(afs.path()).willReturn(index);
		given(afs.size()).willReturn(size);
	}
	
	@After
	public void after() throws Exception {
		Path path = pathFor(indexSha1);
		if (path != null && Files.exists(path)) {
			Files.delete(path);
		}
	}

	@Test
	public void testUpToDate() throws Exception {
		
		Files.write(iSha1, (shaKey + size).getBytes(US_ASCII));
		
		Sha1Resource resource = new Sha1Resource(dependencies, new Sha1ResourceCreator.Sha1ResourceTarget(afs));
		
		assertThat(resource.representedSha(), is(shaKey));
		assertThat(resource.representedFileSize(), is(size));
		
		verify(afs).addDependent(resource);
	}
	
	@Test
	public void testOutOfDate() throws Exception {
		
		Files.write(iSha1, (shaKey + (size - 1L)).getBytes(US_ASCII));
		
		Sha1Resource resource = new Sha1Resource(dependencies, new Sha1ResourceCreator.Sha1ResourceTarget(afs));
		
		assertThat(resource.representedSha(), is(shaKey));
		assertThat(resource.representedFileSize(), is(size));
		
		verify(afs).addDependent(resource);
	}
	
	@Test
	public void testNonExistent() throws Exception {
		
		Sha1Resource resource = new Sha1Resource(dependencies, new Sha1ResourceCreator.Sha1ResourceTarget(afs));
		
		assertThat(resource.representedSha(), is(shaKey));
		assertThat(resource.representedFileSize(), is(size));
		
		assertTrue(Files.exists(iSha1));
		
		verify(afs).addDependent(resource);
	}
}
