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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jj.configuration.Configuration;
import jj.execution.JJExecutors;

import org.junit.After;
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
	
	ResourceCacheImpl resourceCache;
	@Mock ResourceFinder resourceFinder;
	ExecutorService executorService;
	@Mock JJExecutors executors;
	
	ResourceWatchServiceImpl rwsi;
	
	@Before
	public void before() throws Exception {
		
		given(configuration.basePath()).willReturn(Paths.get(getClass().getResource("index.html").toURI()).getParent());
		
		executorService = Executors.newFixedThreadPool(2);
		given(executors.ioExecutor()).willReturn(executorService);
	}
	
	@After
	public void after() throws Exception {
		executorService.shutdownNow();
	}
	
	private void touch(Path path) throws Exception {
		FileTime originalFileTime = Files.getLastModifiedTime(path);
		FileTime newFileTime;
		do {
			newFileTime = FileTime.fromMillis(System.currentTimeMillis());
		} while (newFileTime.compareTo(originalFileTime) < 1);
		
		Files.setLastModifiedTime(path, newFileTime);
	}
	
	private StaticResource make(StaticResourceCreator src, String name) throws Exception {
		return new StaticResource(src.cacheKey(name), configuration.basePath().resolve(name), name);
	}

	@Test
	public void testReplacement() throws Exception {
		

		resourceCache = new ResourceCacheImpl(MockResourceCreators.realized(configuration));
		
		StaticResource sr = make(MockResourceCreators.src, "index.html");
		StaticResource sr1 = make(MockResourceCreators.src, "url_replacement_output.txt");
		StaticResource sr2 = make(MockResourceCreators.src, "sox-icon.png");
		StaticResource sr3 = make(MockResourceCreators.src, "replacement.css");
		StaticResource sr4 = make(MockResourceCreators.src, "images/rox-icon.png");
		sr1.dependsOn(sr2);
		sr3.dependsOn(sr1);
		sr4.dependsOn(sr1);
		String name = "index";
		HtmlResource hr = new HtmlResource(MockResourceCreators.hrc.cacheKey(name), name, configuration.basePath().resolve(name + ".html"));
		
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
