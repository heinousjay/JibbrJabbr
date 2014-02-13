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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;
import jj.execution.JJTask;
import jj.execution.MockTaskRunner;

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

	MockTaskRunner taskRunner;
	@Mock ResourceFinder resourceFinder;
	
	ResourceLoader rl;
	
	@Mock JJTask task;
	
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		rl = new ResourceLoader(taskRunner, resourceFinder);
	}
	
	@Test
	public void test() throws Exception {
		
		rl.loadResource(task, Resource.class, "name", "a thing", new Integer(1));
		
		taskRunner.runFirstTask();
		
		verify(resourceFinder).loadResource(Resource.class, "name", "a thing", new Integer(1));
		
		assertThat(taskRunner.firstTask(), is(task));
	}

}
