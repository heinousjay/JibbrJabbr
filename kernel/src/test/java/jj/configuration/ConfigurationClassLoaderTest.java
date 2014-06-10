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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationClassLoaderTest {
	
	public interface ConfigurationInterface {
		
		String something();
		
		int otherThing();
		
		@Default("default")
		Object defaultedThing();
	}
	
	ConfigurationClassLoader ccl;
	@Mock ConfigurationCollector collector;
	
	@Before
	public void before() throws Exception {
		ccl = new ConfigurationClassLoader();
	}

	@Test
	public void test() throws Exception {
		
		assertThat(ccl.makeClassFor(ConfigurationInterface.class), is(notNullValue()));
	}
	
	
	@Test
	public void testClassCreation() throws Exception {
		
		System.out.println(int.class.getName());
		
		Class<? extends ConfigurationInterface> clazz = ccl.makeConfigurationClassFor(ConfigurationInterface.class);
		
		ConfigurationInterface iface1 = 
			clazz.getConstructor(ConfigurationCollector.class)
				.newInstance(collector);
		
		assertThat(iface1, is(notNullValue()));
		
		String base = ConfigurationInterface.class.getName() + ".";
		given(collector.get(base + "something", String.class, null)).willReturn("something");
		given(collector.get(base + "otherThing", Integer.class, null)).willReturn(45);
		given(collector.get(base + "defaultedThing", Object.class, "default")).willReturn("default");
		assertThat(iface1.something(), is("something"));
		assertThat(iface1.otherThing(), is(45));
		assertThat(iface1.defaultedThing(), is((Object)"default"));
	}

}
