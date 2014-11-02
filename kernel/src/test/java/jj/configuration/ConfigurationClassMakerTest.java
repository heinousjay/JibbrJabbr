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
	
	static final String COMPLICATED_VALUE = "THIS WORKED!";
	
	public interface ConfigurationInterface {
		
		String something();
		
		@Default("true")
		boolean boolThing();
		
		boolean boolOtherThing();
		
		byte byteThing();
		
		short shortThing();
		
		int intThing();

		@Default("-287254729373")
		long longThing();
		
		float floatThing();
		
		double doubleThing();
		
		@Default("default")
		Object defaultedThing();
		
		int undefinedThing();
		
		String otherUndefinedThing();
		
		@DefaultProvider(ComplicatedDefaultProvider.class)
		String complicatedDefault();
	}
	
	@InjectMocks ConfigurationClassMaker ccl;
	@Mock ConfigurationCollector collector;
	
	@Test
	public void testClassCreation() throws Exception {
		
		Class<? extends ConfigurationInterface> clazz = ccl.make(ConfigurationInterface.class);
		
		assertTrue(clazz.isAnnotationPresent(Singleton.class));
		
		Constructor<? extends ConfigurationInterface> ctor = clazz.getConstructor(ConfigurationCollector.class, ComplicatedDefaultProvider.class);
		
		assertTrue(ctor.isAnnotationPresent(Inject.class));
		
		String base = ConfigurationInterface.class.getName() + ".";
		given(collector.get(base + "something", String.class, null)).willReturn("something");
		given(collector.get(base + "intThing", Integer.class, null)).willReturn(45);
		given(collector.get(base + "longThing", Long.class, "-287254729373")).willReturn(-287254729373L);
		given(collector.get(base + "defaultedThing", Object.class, "default")).willReturn("default");
		given(collector.get(base + "complicatedDefault", String.class, COMPLICATED_VALUE)).willReturn(COMPLICATED_VALUE);
		
		ConfigurationInterface iface1 = ctor.newInstance(collector, new ComplicatedDefaultProvider());
		ConfigurationInterface iface2 = ctor.newInstance(collector, new ComplicatedDefaultProvider());
		
		assertThat(iface1, is(notNullValue()));
		assertThat(iface2, is(notNullValue()));
		assertThat(iface1.hashCode(), is(iface2.hashCode()));
		
		assertThat(iface1.something(), is("something"));
		assertThat(iface1.intThing(), is(45));
		assertThat(iface1.defaultedThing(), is((Object)"default"));
		assertThat(iface1.undefinedThing(), is(0));
		assertThat(iface1.longThing(), is(-287254729373L));
		assertThat(iface1.otherUndefinedThing(), is(nullValue()));
		assertThat(iface1.complicatedDefault(), is(COMPLICATED_VALUE));
		
		int hashcode = iface1.hashCode();
		
		// validate that shortcut equality works
		// at least mostly!

		given(collector.get(base + "intThing", Integer.class, null)).willReturn(null);
		given(collector.get(base + "shortThing", Short.class, null)).willReturn((short)45);
		
		assertThat(iface1.hashCode(), is(not(hashcode)));
		
		given(collector.get(base + "intThing", Integer.class, null)).willReturn(45);
		given(collector.get(base + "shortThing", Short.class, null)).willReturn(null);
		
		assertThat(iface1.hashCode(), is(hashcode));
	}

}
