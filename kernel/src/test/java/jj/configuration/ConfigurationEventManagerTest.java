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

import static jj.application.AppLocation.AppBase;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.CONFIG_SCRIPT_NAME;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.CONFIG_NAME;
import static jj.server.ServerLocation.Virtual;

import jj.Base;
import jj.MockServerStarting;
import jj.ServerStarting;
import jj.ServerStarting.Priority;
import jj.execution.TaskHelper;
import jj.resource.MockPathCreation;
import jj.resource.PathResolver;
import jj.resource.ResourceLoader;

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
public class ConfigurationEventManagerTest {

	@Mock ResourceLoader resourceLoader;

	@Mock PathResolver pathResolver;

	ConfigurationEventManager cem;

	@Before
	public void before() {
		given(pathResolver.resolvePath(AppBase, CONFIG_SCRIPT_NAME)).willReturn(Base.path.resolve(CONFIG_SCRIPT_NAME));
		cem = new ConfigurationEventManager(resourceLoader, pathResolver);
	}

	@Test
	public void testPathCreation() {

		cem.on(new MockPathCreation(Base.path));

		verify(resourceLoader, never()).loadResource(ConfigurationScriptEnvironment.class, Virtual, CONFIG_NAME);

		cem.on(new MockPathCreation(Base.path.resolve(CONFIG_SCRIPT_NAME)));

		verify(resourceLoader).loadResource(ConfigurationScriptEnvironment.class, Virtual, CONFIG_NAME);
	}
	
	@Test
	public void testServerStarting() throws Exception {
		// trips the latch
		cem.on((ConfigurationLoaded) null);

		MockServerStarting event = new MockServerStarting();
		cem.on(event);
		
		assertThat(event.priority, is(Priority.Middle));
		
		TaskHelper.invoke(event.task);
		
		verify(resourceLoader).loadResource(ConfigurationScriptEnvironment.class, Virtual, CONFIG_NAME);
	}

}
