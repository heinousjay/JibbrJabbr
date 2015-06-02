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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static jj.application.AppLocation.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.*;
import jj.application.Application;
import jj.event.MockPublisher;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.ScriptEnvironment;
import jj.script.ScriptEnvironmentInitialized;
import jj.script.module.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationScriptEnvironmentTest {
	
	MockAbstractScriptEnvironmentDependencies dependencies;
	ResourceFinder resourceFinder;
	MockPublisher publisher;
	@Mock ScriptableObject global;
	@Mock ConfigurationCollector collector;
	@Mock Application application;
	
	ConfigurationScriptEnvironment cse;
	
	@Mock ScriptResource configScript;
	
	@Before
	public void before() {
		
		dependencies = new MockAbstractScriptEnvironmentDependencies();

		given(dependencies.mockRhinoContextProvider().context.newObject(global)).willReturn(global);
		given(dependencies.mockRhinoContextProvider().context.newChainedScope(global)).willReturn(global);
		
		resourceFinder = dependencies.resourceFinder();
		publisher = dependencies.publisher();
	}

	@Test
	public void testInitialization() {
		given(resourceFinder.loadResource(ScriptResource.class, AppBase, CONFIG_SCRIPT_NAME)).willReturn(configScript);
		
		cse = new ConfigurationScriptEnvironment(dependencies, global, collector, application);
		
		verify(configScript).addDependent(cse);
		
		// make sure it only triggers on its own initialization
		cse.on(new ScriptEnvironmentInitialized(mock(ScriptEnvironment.class)));

		verify(collector, never()).configurationComplete();
		
		assertThat(publisher.events.size(), is(2));
		assertTrue(publisher.events.get(0) instanceof ConfigurationLoading);
		assertTrue(publisher.events.get(1) instanceof ConfigurationFound);
		
		cse.on(new ScriptEnvironmentInitialized(cse));
		
		verify(collector).configurationComplete();
		
		assertThat(publisher.events.size(), is(3));
		assertTrue(publisher.events.get(2) instanceof ConfigurationLoaded);
	}
	
	@Test
	public void testDefaultConfiguration() {
		
		try {
			cse = new ConfigurationScriptEnvironment(dependencies, global, collector, application);
			fail("should have thrown");
		} catch (NoSuchResourceException nsre) {
			assertThat(nsre, is(notNullValue()));
		}
		
		verify(collector).configurationComplete();
		assertThat(publisher.events.size(), is(3));
		assertTrue(publisher.events.get(0) instanceof ConfigurationLoading);
		assertTrue(publisher.events.get(1) instanceof UsingDefaultConfiguration);
		assertTrue(publisher.events.get(2) instanceof ConfigurationLoaded);
	}

}
