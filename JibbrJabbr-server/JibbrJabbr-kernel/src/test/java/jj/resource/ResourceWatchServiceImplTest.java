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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import jj.configuration.Configuration;
import jj.execution.JJExecutors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * this test is slightly concrete, on purpose
 * 
 * 
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceWatchServiceImplTest {
	
	@Mock Configuration configuration;
	
	ResourceCache resourceCache;
	@Mock ResourceFinder resourceFinder;
	@Mock JJExecutors executors;
	
	ResourceWatchServiceImpl rwsi;
	
	@Before
	public void before() throws Exception {
		
		given(configuration.basePath()).willReturn(Paths.get(getClass().getResource("index.html").toURI()).getParent());
		
		given(executors.ioExecutor()).willReturn(Executors.newFixedThreadPool(2));
	}
	
	private void touch(Path path) throws Exception {
		FileTime originalFileTime = Files.getLastModifiedTime(path);
		FileTime newFileTime;
		do {
			newFileTime = FileTime.fromMillis(System.currentTimeMillis());
		} while (newFileTime.compareTo(originalFileTime) < 1);
		
		Files.setLastModifiedTime(path, newFileTime);
	}

	@Test
	public void testReplacement() throws Exception {
		
		StaticResourceCreator src = new StaticResourceCreator(configuration);
		StaticResource sr = src.create("index.html");
		StaticResource sr1 = src.create("url_replacement_output.txt");
		StaticResource sr2 = src.create("sox-icon.png");
		StaticResource sr3 = src.create("replacement.css");
		StaticResource sr4 = src.create("images/rox-icon.png");
		sr1.dependsOn(sr2);
		sr3.dependsOn(sr1);
		sr4.dependsOn(sr1);
		HtmlResourceCreator hrc = new HtmlResourceCreator(configuration);
		HtmlResource hr = hrc.create("index");
		
		Set<ResourceCreator<? extends Resource>> resourceCreators = new HashSet<>();
		resourceCreators.add(src);
		resourceCreators.add(hrc);
		
		resourceCache = new ResourceCache(resourceCreators);
		
		resourceCache.put(sr.cacheKey(), sr);
		resourceCache.put(hr.cacheKey(), hr);
		resourceCache.put(sr1.cacheKey(), sr1);
		resourceCache.put(sr2.cacheKey(), sr2);
		resourceCache.put(sr3.cacheKey(), sr3);
		resourceCache.put(sr4.cacheKey(), sr4);
		int fileCount = resourceCache.size();
		
		rwsi = new ResourceWatchServiceImpl(resourceCache, resourceFinder, executors);

		rwsi.start();
		try {
			rwsi.watch(sr);
			rwsi.watch(hr);
			
			touch(sr.path());
			touch(sr2.path());
			
			// we need to give it 10+ seconds on the mac.  stupid mac
			// but if there's a watch implementation that notifies quickly,
			// this should trip immediately.  like on the linux build server,
			// seems to work there.  so i'm happy
			if (!setUpLatch(fileCount).await(11, SECONDS)) {
				fail("timed out before notified");
			}
			
		} finally {
			rwsi.stop();
		}
		
		verify(resourceFinder).loadResource(StaticResource.class, "index.html");
		verify(resourceFinder).loadResource(HtmlResource.class, "index");
		verify(resourceFinder).loadResource(StaticResource.class, "url_replacement_output.txt");
		verify(resourceFinder).loadResource(StaticResource.class, "sox-icon.png");
		verify(resourceFinder).loadResource(StaticResource.class, "replacement.css");
		verify(resourceFinder).loadResource(StaticResource.class, "images/rox-icon.png");
	}

	@SuppressWarnings("unchecked")
	private CountDownLatch setUpLatch(int fileCount) {
		final CountDownLatch latch = new CountDownLatch(fileCount);
		
		given(resourceFinder.loadResource(any(Class.class), anyString(), anyVararg())).willAnswer(new Answer<Resource>() {

			@Override
			public Resource answer(InvocationOnMock invocation) throws Throwable {
				latch.countDown();
				return null;
			}
			
		});
		return latch;
	}
}
