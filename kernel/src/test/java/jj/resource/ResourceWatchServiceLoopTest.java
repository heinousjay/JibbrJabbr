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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import jj.Base;
import jj.ServerStarting;
import jj.ServerStopping;
import jj.application.AppLocation;
import jj.event.MockPublisher;
import jj.execution.JJTask;
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

	@Mock ResourceWatchSwitch resourceWatchSwitch;
	ResourceCache resourceCache;
	@Mock ResourceLoader resourceLoader;
	@Mock FileWatcher watcher;
	MockPublisher publisher;
	MockTaskRunner taskRunner;

	Map<Path, FileWatcher.Action> changes = new HashMap<>();
	
	ResourceWatchServiceLoop loop;

	MyResource resource1;
	MyResource resource2;
	MyResource resource3;
	MyResource resource4;
	MyResource resource5;

	@SuppressWarnings({"unchecked", "rawtypes"})
	private ResourceCreators makeResourceCreators() {
		Map map = new HashMap<>();
		map.put(MyResource.class, new MyResourceCreator(publisher = new MockPublisher()));
		return new ResourceCreators(map);
	}
	
	private MyResource makeResource(String name) {
		MyResource result = spy(new MyResource(name, publisher));
		resourceCache.putIfAbsent(result.cacheKey(), result);
		return result;
	}
	
	@Before
	public void before() throws Exception {

		resourceCache = new ResourceCache(makeResourceCreators());
		
		taskRunner = new MockTaskRunner();
		loop = new ResourceWatchServiceLoop(
			resourceWatchSwitch,
			resourceCache,
			resourceLoader,
			watcher,
			publisher,
			taskRunner
		);

		resource1 = makeResource("resource1");
		resource2 = makeResource("resource2");
		resource3 = makeResource("resource3");
		resource4 = makeResource("resource4");
		resource5 = makeResource("resource5");
	}
	
	@Test
	public void testStart() {
		assertThat(startWatchLoop(), is(loop));
	}

	@Mock(extraInterfaces = {FileSystemResource.class}) Resource<?> resource;

	@Test
	public void testWatches() {
		given(resourceWatchSwitch.runFileWatcher()).willReturn(true);
		FileSystemResource fsResource = (FileSystemResource)resource;

		Path path = Paths.get("whatever/makes/you/happy");
		given(fsResource.path()).willReturn(path);

		loop.on(new ResourceLoaded(resource));
		verify(watcher, never()).watch(path.getParent());

		given(watcher.start()).willReturn(true);
		loop.on((ServerStarting) null);
		loop.on(new ResourceLoaded(resource));
		verify(watcher).watch(path.getParent());

		given(fsResource.isDirectory()).willReturn(true);
		loop.on(new ResourceLoaded(resource));
		verify(watcher).watch(path);

	}

	@Test
	public void testDirectoryCreation() throws Exception {
		startWatchLoop();

		// relatively simple, directory creation gets delegated to the loader
		changes.put(Base.path, FileWatcher.Action.Create);
		loopOverChangesAndDie();

		PathCreation pc = (PathCreation)publisher.events.get(0);
		assertThat(pc.path, is(Base.path));
	}
	
	@Test
	public void testDependencyTreeAllDeletes() throws Exception {
		startWatchLoop();
		
		// we should only delete because only resource 5 is set to be reloaded
		// and it is not in the tree as resource one depends on it
		given(resource5.removeOnReload()).willReturn(false);
		
		// set up some dependencies, note a change, and verify!
		resource1.addDependent(resource2);
		resource2.addDependent(resource3);
		resource2.addDependent(resource4);
		resource3.addDependent(resource4);
		resource5.addDependent(resource1);
		
		changes.put(Paths.get(resource1.uri()), FileWatcher.Action.Delete);

		loopOverChangesAndDie();
		
		assertThat(resourceCache.get(resource1.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource2.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource3.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource4.cacheKey()), is(nullValue()));
		assertThat(MyResource.class.cast(resourceCache.get(resource5.cacheKey())), is(resource5));
		
		assertThat(taskRunner.tasks, is(empty()));
		verifyZeroInteractions(resourceLoader);

		verify(resource1).kill();
		verify(resource2).kill();
		verify(resource3).kill();
		verify(resource4).kill();
		verify(resource5, never()).kill();
		assertThat(publisher.events.size(), is(4));
		check((ResourceKilled)publisher.events.get(0), resource1);
		check((ResourceKilled)publisher.events.get(1), resource2);
		check((ResourceKilled)publisher.events.get(2), resource3);
		check((ResourceKilled)publisher.events.get(3), resource4);
	}

	private void check(ResourceEvent event, MyResource resource) {
		assertTrue(event.resourceClass == resource.getClass());
	}

	@Test
	public void testDependencyTreeWithReloads() throws Exception {

		startWatchLoop();
		
		// we should only delete because only resource 5 is set to be reloaded
		// and it is not in the tree as resource one depends on it
		given(resource5.removeOnReload()).willReturn(false);
		
		// set up some dependencies, note a change, and verify!
		resource1.addDependent(resource2);
		resource2.addDependent(resource3);
		resource2.addDependent(resource4);
		resource3.addDependent(resource4);
		resource4.addDependent(resource5);
		
		changes.put(Paths.get(resource1.uri()), FileWatcher.Action.Modify);
		
		loopOverChangesAndDie();
		
		assertThat(resourceCache.get(resource1.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource2.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource3.cacheKey()), is(nullValue()));
		assertThat(resourceCache.get(resource4.cacheKey()), is(nullValue()));
		assertThat(MyResource.class.cast(resourceCache.get(resource5.cacheKey())), is(resource5));
		
		verify(resourceLoader).loadResource(resource5.getClass(), resource5.base(), resource5.name(), resource5.creationArg());

		verify(resource1).kill();
		verify(resource2).kill();
		verify(resource3).kill();
		verify(resource4).kill();
		verify(resource5).kill();
		assertThat(publisher.events.size(), is(5));
		check((ResourceKilled) publisher.events.get(0), resource1);
		check((ResourceKilled) publisher.events.get(1), resource2);
		check((ResourceKilled) publisher.events.get(2), resource3);
		check((ResourceKilled) publisher.events.get(3), resource4);
		check((ResourceKilled) publisher.events.get(4), resource5);

		@SuppressWarnings("unchecked")
		Class<MyResource> runtimeClass = (Class<MyResource>)resource5.getClass();
		verify(resourceLoader).loadResource(runtimeClass, AppLocation.AppBase, resource5.name(), null);
	}

	private JJTask<?> startWatchLoop() {
		given(resourceWatchSwitch.runFileWatcher()).willReturn(true);
		given(watcher.start()).willReturn(true);
		loop.on((ServerStarting) null);
		return taskRunner.tasks.remove(0);
	}

	private void loopOverChangesAndDie() throws Exception {
		InterruptedException ie = new InterruptedException();
		given(watcher.awaitChangedPaths()).willReturn(changes).willThrow(ie);

		try {
			loop.on((ServerStarting)null);
			loop.run();
		} catch (InterruptedException caught) {
			assertTrue(ie == caught);
		} finally {
			loop.on((ServerStopping)null);
		}
	}

}
