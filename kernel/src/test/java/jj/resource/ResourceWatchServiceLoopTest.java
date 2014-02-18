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
import java.util.HashMap;
import java.util.Map;

import jj.configuration.AppLocation;
import jj.execution.MockTaskRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

// this class is relatively concrete because it
// integrates a bit
@RunWith(MockitoJUnitRunner.class)
public class ResourceWatchServiceLoopTest {
	
	ResourceCache resourceCache;
	@Mock ResourceFinder resourceFinder;
	@Mock ResourceWatcher watcher;
	MockTaskRunner taskRunner;

	Map<URI, Boolean> changes = new HashMap<>();
	
	ResourceWatchServiceLoop loop;
	
	URI uri1;
	URI uri2;
	URI uri3;
	URI uri4;
	URI uri5;
	MyResource resource1;
	MyResource resource2;
	MyResource resource3;
	MyResource resource4;
	MyResource resource5;

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ResourceCreators makeResourceCreators() {
		Map map = new HashMap<>();
		map.put(MyResource.class, new MyResourceCreator());
		return new ResourceCreators(map);
	}
	
	private MyResource makeResource(URI uri) {
		MyResource result = spy(new MyResource(uri));
		resourceCache.putIfAbsent(result.cacheKey(), result);
		return result;
	}
	
	@Before
	public void before() throws Exception {
		resourceCache = new ResourceCacheImpl(makeResourceCreators(), null);
		
		taskRunner = new MockTaskRunner();
		loop = new ResourceWatchServiceLoop(resourceCache, resourceFinder, watcher, taskRunner);
		
		
		uri1 = URI.create("resource1");
		resource1 = makeResource(uri1);
		
		uri2 = URI.create("resource1");
		resource2 = makeResource(uri2);
		
		uri3 = URI.create("resource3");
		resource3 = makeResource(uri3);
		
		uri4 = URI.create("resource4");
		resource4 = makeResource(uri4);
		
		uri5 = URI.create("resource5");
		resource5 = makeResource(uri5);
	}
	
	@Test
	public void testDependencyTreeAllDeletes() throws Exception {
		
		// we should only delete because only resource 5 is set to be reloaded
		// and it is not in the tree as resource one depends on it
		given(resource5.removeOnReload()).willReturn(false);
		
		// set up some dependencies, note a change, and verify!
		resource1.addDependent(resource2);
		resource2.addDependent(resource3);
		resource2.addDependent(resource4);
		resource3.addDependent(resource4);
		resource5.addDependent(resource1);
		
		changes.put(uri1, true);
		
		InterruptedException ie = new InterruptedException();
		given(watcher.awaitChangedUris()).willReturn(changes).willThrow(ie);
		
		try {
			loop.run();
		} catch (InterruptedException caught) {
			assertTrue(ie == caught);
		}
		
		assertThat(resourceCache.get(resource1.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource2.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource3.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource4.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource5.cacheKey()), is((Resource)resource5));
		
		assertThat(taskRunner.tasks, is(empty()));
		verifyZeroInteractions(resourceFinder);
		
	}
	
	@Test
	public void testDependencyTreeWithReloads() throws Exception {
		
		// we should only delete because only resource 5 is set to be reloaded
		// and it is not in the tree as resource one depends on it
		given(resource5.removeOnReload()).willReturn(false);
		
		// set up some dependencies, note a change, and verify!
		resource1.addDependent(resource2);
		resource2.addDependent(resource3);
		resource2.addDependent(resource4);
		resource3.addDependent(resource4);
		resource4.addDependent(resource5);
		
		changes.put(uri1, false);
		
		InterruptedException ie = new InterruptedException();
		given(watcher.awaitChangedUris()).willReturn(changes).willThrow(ie);
		
		try {
			loop.run();
		} catch (InterruptedException caught) {
			assertTrue(ie == caught);
		}
		
		assertThat(resourceCache.get(resource1.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource2.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource3.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource4.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource5.cacheKey()), is((Resource)resource5));
		
		assertThat(taskRunner.tasks, is(not(empty())));
		
		taskRunner.runFirstTask();
		
		verify(resourceFinder).loadResource(resource5.getClass(), AppLocation.Base, resource5.name());
	}

}