/*
, *    Copyright 2012 Jason Miller
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
import static jj.system.ServerLocation.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static jj.system.Assets.*;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.App;
import jj.document.DocumentScriptEnvironment;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.http.server.EmbeddedHttpRequest;
import jj.http.server.EmbeddedHttpServer;
import jj.http.server.resource.StaticResource;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.script.module.ModuleScriptEnvironment;
import jj.script.module.RequiredModule;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Before;
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

	private static final String DOCUMENT_ONE = "index";
	private static final String DOCUMENT_TWO = "deep/nested";

	private static final String MODULE_TWO = "deep/nesting/module";
	private static final String MODULE_ONE = "deep/module";

	@Inject ResourceFinder resourceFinder;
	@Inject ResourceLoader resourceLoader;
	@Inject EmbeddedHttpServer server;
	
	@Rule
	public JibbrJabbrTestServer app = 
		new JibbrJabbrTestServer(App.module)
			.injectInstance(this);
	
	CountDownLatch latch;
	
	DocumentScriptEnvironment scriptEnvironment;
	
	AtomicInteger documentOneCount = new AtomicInteger(0);
	AtomicInteger documentTwoCount = new AtomicInteger(0);
	
	@Listener
	void scriptEnvironmentInitialized(ScriptEnvironmentInitialized sei) {
		
		if (sei.scriptEnvironment() instanceof DocumentScriptEnvironment) {
			scriptEnvironment = (DocumentScriptEnvironment)sei.scriptEnvironment();
			latch.countDown();
			
			if (DOCUMENT_ONE.equals(sei.scriptEnvironment().name())) {
				documentOneCount.incrementAndGet();
			} else if (DOCUMENT_TWO.equals(sei.scriptEnvironment().name())) {
				documentTwoCount.incrementAndGet();
			}
		}
	}
	
	private ServerTask countDown(final CountDownLatch latch) {
		return new ServerTask("countdown") {
			
			@Override
			protected void run() throws Exception {
				latch.countDown();
			}
		};
	}
	
	@Before
	public void before() {
		resourceLoader.loadResource(StaticResource.class, Assets, JJ_JS);
		resourceLoader.loadResource(StaticResource.class, Assets, JQUERY_JS);
	}
	
	// there should be a similar test in a resource system integration test
	@Test
	public void testingDuplicateInitializationRequestsJoin() throws Exception {
		
		// external set up
		documentOneCount.set(0);
		documentTwoCount.set(0);
		latch = new CountDownLatch(2);
		
		CountDownLatch latch1 = new CountDownLatch(12);
		ServerTask countDown = countDown(latch1);
		
		String name1 = DOCUMENT_ONE;
		String name2 = DOCUMENT_TWO;
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name1).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name1).then(countDown);
		
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name2).then(countDown);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name2).then(countDown);
		
		assertTrue(latch1.await(1, SECONDS));
		assertTrue(latch.await(1, SECONDS));
		
		assertThat(documentOneCount.get(), is(1));
		assertThat(documentTwoCount.get(), is(1));
	}
	
	@Test
	public void test1() throws Throwable {
		
		loadScriptEnvironment(DOCUMENT_TWO);
		
		assertThat(scriptEnvironment.name(), is(DOCUMENT_TWO));
		assertThat(scriptEnvironment.initialized(), is(true));
		
		// need to run a document request first
		assertThat(server.request(new EmbeddedHttpRequest(DOCUMENT_TWO)).await(1, SECONDS).status(), is(HttpResponseStatus.OK));
		
		ModuleScriptEnvironment mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, Virtual, MODULE_ONE, new RequiredModule(scriptEnvironment, MODULE_ONE));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
		
		mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, Virtual, MODULE_TWO, new RequiredModule(scriptEnvironment, MODULE_TWO));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
	}
	
	@Test
	public void test2() throws Exception {
		
		loadScriptEnvironment("index");
		
		assertThat(scriptEnvironment.name(), is(DOCUMENT_ONE));
		assertThat(scriptEnvironment.initialized(), is(true));
	}
	
	@Test
	public void test3() throws Throwable {
		
		loadScriptEnvironment(DOCUMENT_TWO);
		
		assertThat(scriptEnvironment.name(), is(DOCUMENT_TWO));
		assertThat(scriptEnvironment.initialized(), is(true));
		
		// need to run a document request first
		assertThat(server.request(new EmbeddedHttpRequest(DOCUMENT_TWO)).await(1, SECONDS).status(), is(HttpResponseStatus.OK));
		
		
		ModuleScriptEnvironment mse =
			resourceFinder.findResource(ModuleScriptEnvironment.class, Virtual, MODULE_ONE, new RequiredModule(scriptEnvironment, MODULE_ONE));
		
		assertThat(mse, is(notNullValue()));
		assertThat(mse.initialized(), is(true));
		assertThat(mse.parent(), is((ScriptEnvironment)scriptEnvironment));
		
	}

	/**
	 * @throws InterruptedException
	 */
	private void loadScriptEnvironment(final String name) throws InterruptedException {
		latch = new CountDownLatch(1);
		resourceLoader.loadResource(DocumentScriptEnvironment.class, Virtual, name);
		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

}
