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
import static org.mockito.BDDMockito.given;
import jj.conversion.Converters;
import jj.resource.ConfigResource;
import jj.resource.ConfigResourceMaker;
import jj.resource.ResourceFinder;
import jj.script.RealRhinoContextMaker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ConfigurationObjectBaseTest {
	
	Arguments arguments = new Arguments(new String[0]);
	Converters converters = new Converters();
	@Mock ResourceFinder resourceFinder;
	
	class HttpServerSocketConfiguration extends ConfigurationObjectBase {

		HttpServerSocketConfiguration() {
			super(arguments, converters, resourceFinder, new RealRhinoContextMaker());
		}

		@Override
		protected Scriptable configureScriptObject() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Before
	public void before() throws Exception {

		
		ConfigResource resource = ConfigResourceMaker.configResource();
		
		given(resourceFinder.findResource(ConfigResource.class, ConfigResource.CONFIG_JS)).willReturn(resource);
	}

	@Test
	public void testName() {
		
		ConfigurationObjectBase toTest = new HttpServerSocketConfiguration();
		
		assertThat(toTest.name(), is("httpServerSocket"));
	}

}
