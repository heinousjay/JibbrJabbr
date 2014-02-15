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

import static org.mockito.BDDMockito.*;

import javax.inject.Provider;

import jj.configuration.Arguments;
import jj.execution.TaskRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ResourceWatchServiceImplTest {

	@Mock Provider<ResourceWatchServiceLoop> loopProvider;
	@Mock ResourceWatchServiceLoop loop;
	@Mock TaskRunner taskRunner;
	@Mock Arguments arguments;
	@Mock FileResource resource;
	
	@Test
	public void testNoRun() {
		// given
		given(arguments.get("fileWatcher", boolean.class, true)).willReturn(false);
		
		// when
		ResourceWatchServiceImpl service = new ResourceWatchServiceImpl(taskRunner, arguments, loopProvider);
		service.start();
		
		// then
		verify(loopProvider, never()).get();
		verifyZeroInteractions(taskRunner);
	}
	
	@Test
	public void testRun() throws Exception {

		given(arguments.get("fileWatcher", boolean.class, true)).willReturn(true);
		given(loopProvider.get()).willReturn(loop);
		
		ResourceWatchServiceImpl service = new ResourceWatchServiceImpl(taskRunner, arguments, loopProvider);
		service.start();
		service.watch(resource);
		
		verify(taskRunner).execute(loop);
		verify(loop).watch(resource);
	}
}
