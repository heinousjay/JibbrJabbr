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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jj.conversion.ConverterSetMaker;
import jj.conversion.Converters;
import jj.script.CurrentScriptEnvironment;

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
public class ConfigurationCollectorTest {
	
	@Mock CurrentScriptEnvironment env;
	@Mock ConfigurationScriptEnvironment cse;
	Converters converters;
	ConfigurationCollector collector;

	@Before
	public void before() {
		given(env.currentRootScriptEnvironment()).willReturn(cse);
		
		converters = new Converters(ConverterSetMaker.converters());
		collector = new ConfigurationCollector(converters, env);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void test() {
		
		collector.addConfigurationElement("key1", "value1");
		collector.addConfigurationElement("key2", 2);
		collector.addConfigurationMultiElement("key3", true);
		collector.addConfigurationMultiElement("key3", false);
		collector.addConfigurationMappedElement("key4", "key1", 1);
		collector.addConfigurationMappedElement("key4", "key2", 2);
		
		collector.configurationComplete();
		
		assertThat(collector.get("key1", String.class, null), is("value1"));
		assertThat(collector.get("key2", int.class, 1), is(2));
		
		List<Boolean> list = collector.get("key3", List.class, null);
		
		assertThat(list, is(notNullValue()));
		assertThat(list.size(), is(2));
		assertThat(list.get(0), is(true));
		assertThat(list.get(1), is(false));
		
		list = collector.get("notAKey", List.class, null);
		
		assertThat(list, is(notNullValue()));
		assertTrue(list.isEmpty());
		
		list = collector.get("alsoNotAKey", List.class, Collections.EMPTY_LIST);
		
		assertThat(list, is(notNullValue()));
		assertTrue(list.isEmpty());
		
		Map<String, Integer> map = collector.get("key4", Map.class, null);
		
		assertThat(map, is(notNullValue()));
		assertThat(map.size(), is(2));
		assertThat(map.get("key1"), is(1));
		assertThat(map.get("key2"), is(2));
		
		map = collector.get("notAKey", Map.class, null);
		
		assertThat(map, is(notNullValue()));
		assertTrue(map.isEmpty());
		
		map = collector.get("alsoNotAKey", Map.class, Collections.EMPTY_MAP);
		
		assertThat(map, is(notNullValue()));
		assertTrue(map.isEmpty());
	}

}
