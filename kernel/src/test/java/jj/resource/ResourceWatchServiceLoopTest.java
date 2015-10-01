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
import static jj.server.ServerLocation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import jj.Base;
import jj.ServerStarting;
import jj.ServerStopping;
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
		resourceCache.putIfAbsent(result);
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

	public interface MyTestResource extends FileSystemResource, Resource<Void> {}

	@Mock MyTestResource resource;

	@SuppressWarnings("unchecked")
	@Test
	public void testWatches() {
		given((ResourceIdentifier<MyTestResource, Void>)resource.identifier()).willReturn(ResourceIdentifierHelper.make(MyTestResource.class, Virtual, "whatever"));

		given(resourceWatchSwitch.runFileWatcher()).willReturn(true);

		Path path = Paths.get("whatever/makes/you/happy");
		given(resource.path()).willReturn(path);

		loop.on(new ResourceLoaded(resource));
		verify(watcher, never()).watch(path.getParent());

		given(watcher.start()).willReturn(true);
		loop.on((ServerStarting) null);
		loop.on(new ResourceLoaded(resource));
		verify(watcher).watch(path.getParent());

		given(resource.isDirectory()).willReturn(true);
		loop.on(new ResourceLoaded(resource));
		verify(watcher).watch(path);

	}

	@Test
	public void testDirectoryCreation() throws Exception {
		startWatchLoop();

		changes.put(Base.path, FileWatcher.Action.Create);
		loopOverChangesAndDie();

		DirectoryCreation dc = (DirectoryCreation)publisher.events.get(0);
		assertThat(dc.path, is(Base.path));
	}

	@Test
	public void testFileCreation() throws Exception {
		startWatchLoop();
		Path path = Base.path.resolve("blank.gif");
		changes.put(path, FileWatcher.Action.Create);
		loopOverChangesAndDie();

		FileCreation fc = (FileCreation)publisher.events.get(0);
		assertThat(fc.path, is(path));
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
		
		changes.put(resource1.path(), FileWatcher.Action.Delete);

		loopOverChangesAndDie();
		
		assertThat(resourceCache.get(resource1.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource2.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource3.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource4.identifier()), is(nullValue()));
		assertThat(MyResource.class.cast(resourceCache.get(resource5.identifier())), is(resource5));
		
		assertThat(taskRunner.tasks, is(empty()));
		verifyZeroInteractions(resourceLoader);

		verify(resource1).kill();
		verify(resource2).kill();
		verify(resource3).kill();
		verify(resource4).kill();
		verify(resource5, never()).kill();
		assertThat(publisher.events.size(), is(9));
		check((ResourceKilled)publisher.events.get(5), resource1);
		check((ResourceKilled)publisher.events.get(6), resource2);
		check((ResourceKilled)publisher.events.get(7), resource3);
		check((ResourceKilled)publisher.events.get(8), resource4);
	}

	private void check(ResourceEvent event, MyResource resource) {
		// because we're spying, the resource is a runtime subclass
		assertTrue(event.type().isAssignableFrom(resource.getClass()));
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
		
		changes.put(resource1.path(), FileWatcher.Action.Modify);
		
		loopOverChangesAndDie();
		
		assertThat(resourceCache.get(resource1.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource2.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource3.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource4.identifier()), is(nullValue()));
		assertThat(resourceCache.get(resource5.identifier()), is(nullValue()));

		verify(resource1).kill();
		verify(resource2).kill();
		verify(resource3).kill();
		verify(resource4).kill();
		verify(resource5).kill();
		assertThat(publisher.events.size(), is(10));
		check((ResourceKilled) publisher.events.get(5), resource1);
		check((ResourceKilled) publisher.events.get(6), resource2);
		check((ResourceKilled) publisher.events.get(7), resource3);
		check((ResourceKilled) publisher.events.get(8), resource4);
		check((ResourceKilled) publisher.events.get(9), resource5);

		verify(resourceLoader).loadResource(resource5.identifier());
		verifyNoMoreInteractions(resourceLoader);
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
