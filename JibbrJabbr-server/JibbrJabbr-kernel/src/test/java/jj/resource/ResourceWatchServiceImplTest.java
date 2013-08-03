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

	@Test
	public void test() throws Exception {
		
		Path indexPath = configuration.basePath().resolve("index.html");
		
		StaticResourceCreator src = new StaticResourceCreator(configuration);
		StaticResource sr = src.create("index.html");
		HtmlResourceCreator hrc = new HtmlResourceCreator(configuration);
		HtmlResource hr = hrc.create("index");
		
		Set<ResourceCreator<? extends Resource>> resourceCreators = new HashSet<>();
		resourceCreators.add(src);
		resourceCreators.add(hrc);
		
		resourceCache = new ResourceCache(resourceCreators);
		
		resourceCache.put(src.cacheKey("index.html"), sr);
		resourceCache.put(hrc.cacheKey("index"), hr);
		int fileCount = resourceCache.size();
		
		rwsi = new ResourceWatchServiceImpl(resourceCache, resourceFinder, executors);

		rwsi.start();
		try {
			rwsi.watch(sr);
			rwsi.watch(hr);
			
			FileTime originalFileTime = Files.getLastModifiedTime(indexPath);
			FileTime newFileTime;
			do {
				newFileTime = FileTime.fromMillis(System.currentTimeMillis());
			} while (newFileTime.compareTo(originalFileTime) < 1);
			
			Files.setLastModifiedTime(indexPath, newFileTime);
			
			// we need to give it 10+ seconds on the mac.  stupid mac
			// but if there's a watch implementation that notifies quickly,
			// this should trip immediately
			if (!setUpLatch(fileCount).await(11, SECONDS)) {
				fail("timed out before notified");
			}
			
		} finally {
			rwsi.stop();
		}
		
		verify(resourceFinder).loadResource(StaticResource.class, "index.html");
		verify(resourceFinder).loadResource(HtmlResource.class, "index");
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
