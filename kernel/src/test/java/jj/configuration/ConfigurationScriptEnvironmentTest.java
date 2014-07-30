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
import static jj.configuration.resolution.AppLocation.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.ConfigurationScriptEnvironmentCreator.*;
import jj.event.Publisher;
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
	@Mock ResourceFinder resourceFinder;
	@Mock Publisher publisher;
	@Mock ScriptableObject global;
	@Mock ConfigurationCollector collector;
	
	ConfigurationScriptEnvironment cse;
	
	@Mock ScriptResource configScript;
	
	@Before
	public void before() {
		
		dependencies = new MockAbstractScriptEnvironmentDependencies();

		given(dependencies.rhinoContextProvider().context.newObject(global)).willReturn(global);
		given(dependencies.rhinoContextProvider().context.newChainedScope(global)).willReturn(global);
	}

	@Test
	public void testInitialization() {
		given(resourceFinder.loadResource(ScriptResource.class, Base, CONFIG_SCRIPT_NAME)).willReturn(configScript);
		
		cse = new ConfigurationScriptEnvironment(
			dependencies,
			resourceFinder, publisher, global, collector
		);
		
		verify(publisher).publish(isA(ConfigurationLoading.class));
		verify(configScript).addDependent(cse);
		verify(publisher).publish(isA(ConfigurationFound.class));
		
		// make sure it only triggers on its own initialization
		cse.scriptInitialized(new ScriptEnvironmentInitialized(mock(ScriptEnvironment.class)));

		verify(collector, never()).configurationComplete();
		verify(publisher, never()).publish(isA(ConfigurationLoaded.class));
		
		cse.scriptInitialized(new ScriptEnvironmentInitialized(cse));
		
		verify(collector).configurationComplete();
		verify(publisher).publish(isA(ConfigurationLoaded.class));
	}
	
	@Test
	public void testDefaultConfiguration() {
		
		try {
			cse = new ConfigurationScriptEnvironment(
				dependencies,
				resourceFinder, publisher, global, collector
			);
			fail("should have thrown");
		} catch (NoSuchResourceException nsre) {
			assertThat(nsre, is(notNullValue()));
		}
		
		verify(publisher).publish(isA(UsingDefaultConfiguration.class));
		verify(collector).configurationComplete();
		verify(publisher).publish(isA(ConfigurationLoaded.class));
	}

}
