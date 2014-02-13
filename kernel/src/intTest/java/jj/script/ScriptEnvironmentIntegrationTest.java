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
package jj.script;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.RequiredModule;
import jj.testing.App;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Rule;
import org.junit.Test;

/**
 * loads up a few script environments to do the nasty
 * 
 * @author jason
 *
 */
@Subscriber
public class ScriptEnvironmentIntegrationTest {
	
	@Inject ResourceFinder resourceFinder;
	@Inject ResourceLoader resourceLoader;
	
	@Rule
	public JibbrJabbrTestServer app = 
		new JibbrJabbrTestServer(App.path1)
			.injectInstance(this);
	
	CountDownLatch latch;
	
	DocumentScriptEnvironment scriptEnvironment;
	
	@Listener
	void scriptEnvironmentInitialized(ScriptEnvironmentInitialized sei) {
		if (sei.scriptEnvironment() instanceof DocumentScriptEnvironment) {
			scriptEnvironment = (DocumentScriptEnvironment)sei.scriptEnvironment();
			latch.countDown();
		}
	}
	
	@Test
	public void test() throws Exception {
		
		loadScriptEnvironment("animal");
		
		assertThat(scriptEnvironment.baseName(), is("animal"));
		assertThat(scriptEnvironment.initialized(), is(true));
		
		// these lines just validate that the animal ScriptEnvironment correctly loaded and initialized its dependencies
		
		ModuleScriptEnvironment mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, "modules/module1", new RequiredModule(scriptEnvironment, "modules/module1"));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
		
		mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, "modules/module2", new RequiredModule(scriptEnvironment, "modules/module2"));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
	}
	
	@Test
	public void test2() throws Exception {
		
		loadScriptEnvironment("index");
		
		assertThat(scriptEnvironment.baseName(), is("index"));
		assertThat(scriptEnvironment.initialized(), is(true));
	}

	/**
	 * @throws InterruptedException
	 */
	private void loadScriptEnvironment(final String name) throws InterruptedException {
		latch = new CountDownLatch(1);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, name);
		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

}