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
import jj.execution.IOTask;
import jj.execution.JJExecutor;
import jj.resource.ResourceFinder;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.RequiredModule;
import jj.testing.App;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Before;
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
	@Inject JJExecutor executor;
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.path1);
	
	CountDownLatch latch;
	
	DocumentScriptEnvironment scriptEnvironment;
	
	@Before
	public void before() {
		resourceFinder = null;
		executor = null;
		app.inject(this);
	}
	
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

	/**
	 * @throws InterruptedException
	 */
	private void loadScriptEnvironment(final String name) throws InterruptedException {
		latch = new CountDownLatch(1);
		
		executor.execute(new IOTask("loading the script") {
			@Override
			protected void run() throws Exception {
				resourceFinder.loadResource(DocumentScriptEnvironment.class, name);
			}
		});
		
		assertTrue(latch.await(2, TimeUnit.SECONDS));
	}

}
