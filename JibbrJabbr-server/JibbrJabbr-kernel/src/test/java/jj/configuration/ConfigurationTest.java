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
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.CoreConfiguration;
import jj.configuration.Configuration;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ConfigurationTest {
	
	Path realPath;
	Configuration toTest;

	@Before 
	public void before() throws Exception {
		realPath = Paths.get(getClass().getResource("/index.html").toURI()).getParent();
		toTest = new Configuration(new String[] {realPath.toString()});
	}
	
	@Test
	public void test() {
		
		
		assertThat(toTest.appPath(), is(realPath));
	}
	
	@Test
	public void testRetrieveConfigurationInstances() throws Exception {
		CoreConfiguration instance = toTest.get(CoreConfiguration.class);
		
		assertThat(instance, is(notNullValue()));
		System.out.println(instance.getClass().getName());
		
		assertThat(instance.appPath(), is(realPath));
	}
	
	@Test
	public void testIsSystemRunning() {
		assertThat(toTest.isSystemRunning(), is(true));
		assertThat(mock(Configuration.class).isSystemRunning(), is(false));
	}

}
