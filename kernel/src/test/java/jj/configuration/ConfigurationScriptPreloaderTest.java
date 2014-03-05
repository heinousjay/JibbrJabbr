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
package jj.configuration;

import static org.mockito.BDDMockito.*;
import jj.event.Publisher;
import jj.execution.MockTaskRunner;
import jj.resource.ResourceFinder;
import jj.resource.config.ConfigResource;

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
public class ConfigurationScriptPreloaderTest {

	MockTaskRunner taskRunner;
	@Mock ResourceFinder resourceFinder;
	@Mock Publisher publisher;
	
	@Mock ConfigResource resource;
	
	ConfigurationScriptPreloader csp;
	
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		csp = new ConfigurationScriptPreloader(taskRunner, resourceFinder, publisher);
	}
	
	@Test
	public void testDefault() throws Exception {
		
		csp.start();
		taskRunner.runFirstTask();
		
		verify(resourceFinder).loadResource(ConfigResource.class, AppLocation.Base, ConfigResource.CONFIG_JS);
		
		verify(publisher).publish(isA(UsingDefaultConfiguration.class));
	}
	
	@Test
	public void testFound() throws Exception {
		
		given(resourceFinder.loadResource(ConfigResource.class, AppLocation.Base, ConfigResource.CONFIG_JS)).willReturn(resource);

		csp.start();
		taskRunner.runFirstTask();
		
		verify(publisher).publish(isA(ConfigurationFoundEvent.class));
	}

}
