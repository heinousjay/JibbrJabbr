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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ResourceWatchServiceImplTest {

	@Mock ResourceWatchServiceLoop loop;
	@Mock ResourceWatchSwitch resourceWatchSwitch;
	@Mock DirectoryResource resource;
	
	@Test
	public void testNoRun() throws Exception {
		// given
		given(resourceWatchSwitch.runFileWatcher()).willReturn(false); // just to be explicit
		
		// when
		ResourceWatchServiceImpl service = new ResourceWatchServiceImpl(resourceWatchSwitch, loop);
		service.on(null);
		service.watch(resource);
		
		// then
		verify(loop).stop();
		verifyNoMoreInteractions(loop);
	}
	
	@Test
	public void testRun() throws Exception {

		given(resourceWatchSwitch.runFileWatcher()).willReturn(true);

		
		ResourceWatchServiceImpl service = new ResourceWatchServiceImpl(resourceWatchSwitch, loop);
		service.on(null);
		service.watch(resource);
		
		verify(loop).start();
		verify(loop).watch(resource);
	}
}
