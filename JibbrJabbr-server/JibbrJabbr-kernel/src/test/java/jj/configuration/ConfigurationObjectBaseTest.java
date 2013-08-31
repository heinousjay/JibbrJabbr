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
import jj.conversion.ConverterSetMaker;
import jj.conversion.Converters;
import jj.http.server.Binding;
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
	
	Converters converters = new Converters(ConverterSetMaker.converters());
	@Mock ResourceFinder resourceFinder;
	
	// we do this a weird way internally to exhaust the point
	interface HttpServerSocketConfiguration {}
	
	class HttpServerSocketConfigurationObject extends ConfigurationObjectBase implements HttpServerSocketConfiguration {

		HttpServerSocketConfigurationObject() {
			super(ConfigurationObjectBaseTest.this.converters, resourceFinder, new RealRhinoContextMaker());
		}

		@Override
		protected Scriptable configureScriptObject(Scriptable scope) {
			try (RhinoContext context = contextMaker.context()) {
			
				Scriptable nativeObject = context.newObject(scope);
				ScriptableObject.putConstProperty(nativeObject, "keepAlive", 
					configurationFunction("keepAlive", Boolean.TYPE, false, null));
				ScriptableObject.putConstProperty(nativeObject, "tcpNoDelay", 
					configurationFunction("tcpNoDelay", Boolean.TYPE, false, null));
				ScriptableObject.putConstProperty(nativeObject, "backlog", 
					configurationFunction("backlog", Integer.TYPE, false, null));
				ScriptableObject.putConstProperty(nativeObject, "timeout", 
					configurationFunction("timeout", Integer.TYPE, false, null));
				ScriptableObject.putConstProperty(nativeObject, "reuseAddress", 
					configurationFunction("reuseAddress", Boolean.TYPE, false,  null));
				ScriptableObject.putConstProperty(nativeObject, "sendBufferSize", 
					configurationFunction("sendBufferSize", Integer.TYPE, false, null));
				ScriptableObject.putConstProperty(nativeObject, "receiveBufferSize", 
					configurationFunction("receiveBufferSize", Integer.TYPE, false, null));
				
				
				// ehhhhh
				ScriptableObject.putConstProperty(nativeObject, "bind", 
					configurationFunction("binding", Binding.class, true, null));
				
				return nativeObject;
			}
		}
		
		@Override
		protected void setDefaults() {
			// TODO Auto-generated method stub
			
		}
		
		public boolean keepAlive() {
			return (boolean)values.get("keepAlive");
		}
		public boolean tcpNoDelay() {
			return (boolean)values.get("tcpNoDelay");
		}
		public boolean reuseAddress() {
			return (boolean)values.get("reuseAddress");
		}
		public int backlog() {
			return (int)values.get("backlog");
		}
		public int timeout() {
			return (int)values.get("timeout");
		}
		public int sendBufferSize() {
			return (int)values.get("sendBufferSize");
		}
		public int receiveBufferSize() {
			return (int)values.get("receiveBufferSize");
		}
		public Binding binding() {
			return (Binding)values.get("binding");
		}
		
	}
	
	@Before
	public void before() throws Exception {
		
		ConfigResource resource = ConfigResourceMaker.configResource();
		
		given(resourceFinder.findResource(ConfigResource.class, ConfigResource.CONFIG_JS)).willReturn(resource);
	}

	@Test
	public void test() {
		
		HttpServerSocketConfigurationObject toTest = new HttpServerSocketConfigurationObject();
		
		assertThat(toTest.name(), is("httpServerSocket"));
		
		toTest.runScriptFunction();
		
		assertThat(toTest.keepAlive(), is(true));
		assertThat(toTest.tcpNoDelay(), is(true));
		assertThat(toTest.backlog(), is(1024));
		assertThat(toTest.timeout(), is(10000));
		assertThat(toTest.reuseAddress(), is(true));
		assertThat(toTest.sendBufferSize(), is(65536));
		assertThat(toTest.receiveBufferSize(), is(65536));
	}

}
