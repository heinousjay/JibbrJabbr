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
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class ConfigurationObjectBaseTest {
	
	Arguments arguments = new Arguments(new String[0]);
	Converters converters = new Converters();
	@Mock ResourceFinder resourceFinder;
	
	// we do this a weird way internally to exhaust the point
	interface HttpServerSocketConfiguration {}
	
	class HttpServerSocketConfigurationObject extends ConfigurationObjectBase implements HttpServerSocketConfiguration {

		HttpServerSocketConfigurationObject() {
			super(arguments, converters, resourceFinder, new RealRhinoContextMaker());
		}

		@Override
		protected Scriptable configureScriptObject(Scriptable scope) {
			try (RhinoContext context = contextMaker.context()) {
			
				Scriptable nativeObject = context.newObject(scope);
				ScriptableObject.putConstProperty(nativeObject, "keepAlive", configurationFunction("keepAlive"));
				ScriptableObject.putConstProperty(nativeObject, "tcpNoDelay", configurationFunction("tcpNoDelay"));
				ScriptableObject.putConstProperty(nativeObject, "backlog", configurationFunction("backlog"));
				ScriptableObject.putConstProperty(nativeObject, "timeout", configurationFunction("timeout"));
				ScriptableObject.putConstProperty(nativeObject, "reuseAddress", configurationFunction("reuseAddress"));
				ScriptableObject.putConstProperty(nativeObject, "sendBufferSize", configurationFunction("sendBufferSize"));
				ScriptableObject.putConstProperty(nativeObject, "receiveBufferSize", configurationFunction("receiveBufferSize"));
				
				
				// ehhhhh
				ScriptableObject.putConstProperty(nativeObject, "bind", configurationFunction("bind"));
				
				return nativeObject;
			}
		}
		
		
	}
	
	@Before
	public void before() throws Exception {
		
		ConfigResource resource = ConfigResourceMaker.configResource();
		
		given(resourceFinder.findResource(ConfigResource.class, ConfigResource.CONFIG_JS)).willReturn(resource);
	}

	@Test
	public void test() {
		
		ConfigurationObjectBase toTest = new HttpServerSocketConfigurationObject();
		
		assertThat(toTest.name(), is("httpServerSocket"));
		
		toTest.runScriptFunction();
		
		assertThat(toTest.readScriptValue("keepAlive", null, Boolean.TYPE), is(true));
		assertThat(toTest.readScriptValue("tcpNoDelay", null, Boolean.TYPE), is(true));
		assertThat(toTest.readScriptValue("backlog", null, Integer.TYPE), is(1024));
		assertThat(toTest.readScriptValue("timeout", null, Integer.TYPE), is(10000));
		assertThat(toTest.readScriptValue("reuseAddress", null, Boolean.TYPE), is(true));
		assertThat(toTest.readScriptValue("sendBufferSize", null, Integer.TYPE), is(65536));
		assertThat(toTest.readScriptValue("receiveBufferSize", null, Integer.TYPE), is(65536));
	}

}
