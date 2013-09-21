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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jj.configuration.Configuration;
import jj.execution.IOTask;
import jj.execution.JJExecutors;
import jj.execution.MockJJExecutors;
import jj.execution.TaskInvoker;
import jj.http.server.servable.document.DocumentConfiguration;
import jj.http.server.servable.document.MockDocumentConfiguration;
import jj.resource.document.HtmlResource;
import jj.resource.document.HtmlResourceMaker;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceMaker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

/**
 * this test is slightly concrete, on purpose
 * 
 * 
 * @author jason
 *
 */
@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class ResourceWatchServiceImplTest {
	
	
	
	String fileName = "gibberish.txt";
	Path gibberish;
	
	@Mock Configuration configuration;
	
	ResourceMaker resourceMaker;
	
	ResourceCacheImpl resourceCache;
	@Mock ResourceFinder resourceFinder;
	ExecutorService executorService;
	@Mock JJExecutors executors;
	@Mock Logger logger;
	
	ResourceWatchServiceImpl rwsi;
	
	@Before
	public void before() throws Exception {
		
		Path appPath = Paths.get(getClass().getResource("index.html").toURI()).getParent();
		
		gibberish = appPath.resolve(fileName);
		
		try (SeekableByteChannel channel = Files.newByteChannel(gibberish, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
			channel.write(ByteBuffer.wrap(fileName.getBytes(UTF_8)));
		}
		
		given(configuration.appPath()).willReturn(appPath);
		given(configuration.get(DocumentConfiguration.class)).willReturn(new MockDocumentConfiguration());
		
		
		executorService = Executors.newCachedThreadPool();
		given(executors.execute(any(IOTask.class))).willAnswer(new Answer<Void>() {

			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				executorService.submit(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						TaskInvoker.invoke(((IOTask)invocation.getArguments()[0]));
						return null;
					}
				});
				return null;
			}
		});
		
		resourceMaker = new ResourceMaker(configuration);
		
		Map<Class<? extends Resource>, ResourceCreator<? extends Resource>> map = new HashMap<>();
		map.put(StaticResource.class, StaticResourceMaker.fake(configuration));
		map.put(HtmlResource.class, HtmlResourceMaker.fake(configuration));
		
		resourceCache = new ResourceCacheImpl(new ResourceCreators(map));
	}
	
	@After
	public void after() throws Exception {
		
		try {
			Files.delete(gibberish);
		} catch (Exception e) {}
		
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

	@Test
	public void test() throws Exception {
		
		// all done in one test because it takes 10 seconds per run on the mac
		
		// set up for replacements
		
		StaticResource sr = resourceMaker.makeStatic("index.html");
		StaticResource sr1 = resourceMaker.makeStatic("url_replacement_output.txt");
		StaticResource sr2 = resourceMaker.makeStatic("sox-icon.png");
		StaticResource sr3 = resourceMaker.makeStatic("replacement.css");
		StaticResource sr4 = resourceMaker.makeStatic("images/rox-icon.png");
		sr1.dependsOn(sr2);
		sr3.dependsOn(sr1);
		sr4.dependsOn(sr1);
		// and test circular dependency
		sr1.dependsOn(sr4);
		String name = "index";
		HtmlResource hr = resourceMaker.makeHtml(name);
		
		resourceCache.putIfAbsent(((AbstractResource)sr).cacheKey(), sr);
		resourceCache.putIfAbsent(((AbstractResource)hr).cacheKey(), hr);
		resourceCache.putIfAbsent(((AbstractResource)sr1).cacheKey(), sr1);
		resourceCache.putIfAbsent(((AbstractResource)sr2).cacheKey(), sr2);
		resourceCache.putIfAbsent(((AbstractResource)sr3).cacheKey(), sr3);
		resourceCache.putIfAbsent(((AbstractResource)sr4).cacheKey(), sr4);
		
		// set up for removal
		
		StaticResource sr1_1 = resourceMaker.makeStatic(fileName);
		StaticResource sr1_2 = resourceMaker.makeStatic("test.css");
		StaticResource sr1_3 = resourceMaker.makeStatic("test.less");
		StaticResource sr1_4 = resourceMaker.makeStatic("test2.less");
		sr1_2.dependsOn(sr1_1);
		sr1_3.dependsOn(sr1_2);
		sr1_4.dependsOn(sr1_2);
		// and test circular dependency
		sr1_1.dependsOn(sr1_4);
		resourceCache.putIfAbsent(((AbstractResource)sr1_1).cacheKey(), sr1_1);
		resourceCache.putIfAbsent(((AbstractResource)sr1_2).cacheKey(), sr1_2);
		resourceCache.putIfAbsent(((AbstractResource)sr1_3).cacheKey(), sr1_3);
		resourceCache.putIfAbsent(((AbstractResource)sr1_4).cacheKey(), sr1_4);
		
		int fileCount = resourceCache.size() - 1;
		
		rwsi = new ResourceWatchServiceImpl(resourceCache, resourceFinder, executors);

		// when 
		rwsi.start();
		try {
			rwsi.watch(sr);
			rwsi.watch(sr1);
			rwsi.watch(sr2);
			rwsi.watch(sr3);
			rwsi.watch(sr4);
			rwsi.watch(hr);
			rwsi.watch(sr1_1);
			rwsi.watch(sr1_2);
			rwsi.watch(sr1_3);
			rwsi.watch(sr1_4);
			
			touch(sr.path());
			touch(sr2.path());
			Files.delete(gibberish);
			
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

		// then
		
		// replacement checks
		
		assertTrue(((AbstractResource)hr).isObselete());
		assertTrue(((AbstractResource)sr).isObselete());
		assertTrue(((AbstractResource)sr1).isObselete());
		assertTrue(((AbstractResource)sr2).isObselete());
		assertTrue(((AbstractResource)sr3).isObselete());
		assertTrue(((AbstractResource)sr4).isObselete());
		
		verify(resourceFinder).loadResource(StaticResource.class, "index.html");
		verify(resourceFinder).loadResource(HtmlResource.class, "index");
		verify(resourceFinder).loadResource(StaticResource.class, "url_replacement_output.txt");
		verify(resourceFinder).loadResource(StaticResource.class, "sox-icon.png");
		verify(resourceFinder).loadResource(StaticResource.class, "replacement.css");
		verify(resourceFinder).loadResource(StaticResource.class, "images/rox-icon.png");
		
		// removal checks
		
		assertTrue(((AbstractResource)sr1_2).isObselete());
		assertTrue(((AbstractResource)sr1_3).isObselete());
		assertTrue(((AbstractResource)sr1_4).isObselete());
		
		verify(resourceFinder).loadResource(StaticResource.class, "test.css");
		verify(resourceFinder).loadResource(StaticResource.class, "test.less");
		verify(resourceFinder).loadResource(StaticResource.class, "test2.less");
		
		verifyNoMoreInteractions(resourceFinder);
	}
}
