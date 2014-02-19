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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.configuration.AppLocation;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
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
 * loads up a few script environments to do the nasty.  also tests out the resource system a bit, which is nice
 * 
 * @author jason
 *
 */
@Subscriber
public class ScriptEnvironmentIntegrationTest {
	
	@Inject ResourceFinder resourceFinder;
	@Inject ResourceLoader resourceLoader;
	
	@Rule
	public JibbrJabbrTestServer server = 
		new JibbrJabbrTestServer(App.path1)
			.injectInstance(this);
	
	CountDownLatch latch;
	
	DocumentScriptEnvironment scriptEnvironment;
	
	AtomicInteger animalCount = new AtomicInteger(0);
	AtomicInteger deep_nestedCount = new AtomicInteger(0);
	
	@Listener
	void scriptEnvironmentInitialized(ScriptEnvironmentInitialized sei) {
		
		if (sei.scriptEnvironment() instanceof DocumentScriptEnvironment) {
			scriptEnvironment = (DocumentScriptEnvironment)sei.scriptEnvironment();
			latch.countDown();
			
			if ("animal".equals(sei.scriptEnvironment().name())) {
				animalCount.incrementAndGet();
			} else if ("deep/nested".equals(sei.scriptEnvironment().name())) {
				deep_nestedCount.incrementAndGet();
			}
		}
	}
	
	private ServerTask countDown(final CountDownLatch latch) {
		return new ServerTask("coutdown") {
			
			@Override
			protected void run() throws Exception {
				latch.countDown();
			}
		};
	}
	
	// there should be a similar test in a resource system integration test
	@Test
	public void testingDuplicateInitializationRequestsJoin() throws Exception {
		
		// external set up
		animalCount.set(0);
		deep_nestedCount.set(0);
		latch = new CountDownLatch(2);
		
		CountDownLatch latch1 = new CountDownLatch(12);
		ServerTask countDown = countDown(latch1);
		
		String name1 = "animal";
		String name2 = "deep/nested";
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name1).then(countDown);
		
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name2).then(countDown);
		
		assertTrue(latch1.await(1, SECONDS));
		assertTrue(latch.await(1, SECONDS));
		
		assertThat(animalCount.get(), is(1));
		assertThat(deep_nestedCount.get(), is(1));
	}
	
	@Test
	public void test1() throws Exception {
		
		loadScriptEnvironment("animal");
		
		assertThat(scriptEnvironment.name(), is("animal"));
		assertThat(scriptEnvironment.initialized(), is(true));
		
		// these lines just validate that the animal ScriptEnvironment correctly loaded and initialized its dependencies
		
		ModuleScriptEnvironment mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "modules/module1", new RequiredModule(scriptEnvironment, "modules/module1"));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
		
		mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "modules/module2", new RequiredModule(scriptEnvironment, "modules/module2"));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
	}
	
	@Test
	public void test2() throws Exception {
		
		loadScriptEnvironment("index");
		
		assertThat(scriptEnvironment.name(), is("index"));
		assertThat(scriptEnvironment.initialized(), is(true));
	}
	
	@Test
	public void test3() throws Exception {
		
		loadScriptEnvironment("deep/nested");
		
		assertThat(scriptEnvironment.name(), is("deep/nested"));
		assertThat(scriptEnvironment.initialized(), is(true));
		
		// need to run a document request first
		assertThat(server.get("deep/nested").status(), is(HttpResponseStatus.OK));
		
		
		ModuleScriptEnvironment mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, AppLocation.Virtual, "deep/module", new RequiredModule(scriptEnvironment, "deep/module"));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
		
	}

	/**
	 * @throws InterruptedException
	 */
	private void loadScriptEnvironment(final String name) throws InterruptedException {
		latch = new CountDownLatch(1);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, name);
		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

}
