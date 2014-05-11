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
package jj.jasmine;

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.configuration.resolution.AppLocation.Virtual;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jj.App;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.script.ScriptEnvironmentInitialized;
import jj.script.resource.ModuleScriptEnvironment;
import jj.script.resource.RequiredModule;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Rule;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;

/**
 * @author jason
 *
 */
@Subscriber
public class JasmineIntegrationTest {
	
	@Rule
	public JibbrJabbrTestServer jj = new JibbrJabbrTestServer(App.minimal).injectInstance(this);
	
	@Inject ResourceLoader resourceLoader;
	@Inject ResourceFinder resourceFinder;
	
	CountDownLatch latch = new CountDownLatch(2);
	
	@Listener
	void initialized(ScriptEnvironmentInitialized sei) {
		
		latch.countDown();
	}

	@Test
	public void test() throws Exception {
		
		resourceLoader.loadResource(JasmineScriptEnvironment.class, Virtual, "test-boot");
		assertTrue("timed out", latch.await(1, SECONDS));
		
		JasmineScriptEnvironment jse = 
			resourceFinder.findResource(JasmineScriptEnvironment.class, Virtual, "test-boot");
		ModuleScriptEnvironment jasmine =
			resourceFinder.findResource(ModuleScriptEnvironment.class, Virtual, "jasmine", new RequiredModule(jse, "jasmine"));
		Scriptable exports = (Scriptable)jasmine.exports();
		
		System.out.println("over");
	}

}
