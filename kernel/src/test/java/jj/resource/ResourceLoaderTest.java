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

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.times;

import jj.application.AppLocation;
import jj.execution.MockTaskRunner;

import jj.execution.Promise;
import jj.http.server.resource.StaticResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ResourceLoaderTest {
	
	public interface TestResource extends Resource<Integer> {}

	MockTaskRunner taskRunner;
	@Mock ResourceFinder resourceFinder;
	ResourceIdentifierMaker maker = new MockResourceIdentifierMaker();
	ResourceIdentifier<TestResource, Integer> id1 = maker.make(TestResource.class, AppLocation.AppBase, "name 1", 1);
	ResourceIdentifier<TestResource, Integer> id2 = maker.make(TestResource.class, AppLocation.AppBase, "name 2", 2);
	ResourceIdentifier<StaticResource, Void> id3 = maker.make(StaticResource.class, AppLocation.Public, "static");
	
	ResourceLoader rl;
	
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		rl = new ResourceLoaderImpl(taskRunner, resourceFinder, maker);
	}

	@Test
	public void testFind() throws Exception {
		// return value here doesn't matter
		rl.findResource(id1);
		rl.findResource(id2);
		rl.findResource(id1.resourceClass, id1.base, id1.name, id1.argument);
		rl.findResource(id3.resourceClass, id3.base, id3.name);

		verify(resourceFinder, times(2)).findResource(id1);
		verify(resourceFinder).findResource(id2);
		verify(resourceFinder).findResource(id3);
	}
	
	@Test
	public void testLoad() throws Exception {

		// kinda simple, we just make sure it's going to make its task and schedule it
		// and that the task does the right thing
		
		assertThat(rl.loadResource(id1.resourceClass, id1.base, id1.name, id1.argument), is(instanceOf(Promise.class)));
		assertThat(rl.loadResource(id1), is(instanceOf(Promise.class)));
		assertThat(rl.loadResource(id3.resourceClass, id3.base, id3.name), is(instanceOf(Promise.class)));

		taskRunner.runUntilIdle();
		
		verify(resourceFinder, times(2)).loadResource(id1);
		verify(resourceFinder).loadResource(id3);
	}

}
