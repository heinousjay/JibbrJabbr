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

import java.lang.reflect.Constructor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationClassMakerTest {
	
	public interface ConfigurationInterface {
		
		String something();
		
		int otherThing();
		
		@Default("default")
		Object defaultedThing();
		
		int undefinedThing();
		
		String otherUndefinedThing();
	}
	
	@InjectMocks ConfigurationClassMaker ccl;
	@Mock ConfigurationCollector collector;
	
	@Test
	public void testClassCreation() throws Exception {
		
		Class<? extends ConfigurationInterface> clazz = ccl.make(ConfigurationInterface.class);
		
		assertTrue(clazz.isAnnotationPresent(Singleton.class));
		
		Constructor<? extends ConfigurationInterface> ctor = clazz.getConstructor(ConfigurationCollector.class);
		
		assertTrue(ctor.isAnnotationPresent(Inject.class));
		
		ConfigurationInterface iface1 = ctor.newInstance(collector);
		
		assertThat(iface1, is(notNullValue()));
		
		String base = ConfigurationInterface.class.getName() + ".";
		given(collector.get(base + "something", String.class, null)).willReturn("something");
		given(collector.get(base + "otherThing", Integer.class, null)).willReturn(45);
		given(collector.get(base + "defaultedThing", Object.class, "default")).willReturn("default");
		assertThat(iface1.something(), is("something"));
		assertThat(iface1.otherThing(), is(45));
		assertThat(iface1.defaultedThing(), is((Object)"default"));
		assertThat(iface1.undefinedThing(), is(0));
		assertThat(iface1.otherUndefinedThing(), is(nullValue()));
	}

}
