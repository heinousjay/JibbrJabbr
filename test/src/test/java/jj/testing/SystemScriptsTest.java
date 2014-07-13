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
package jj.testing;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jj.configuration.resolution.AppLocation.APIModules;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.App;
import jj.JJModule;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.jasmine.JasmineTestFailure;
import jj.jasmine.JasmineTestSuccess;
import jj.resource.ResourceLoader;
import jj.script.module.ScriptResource;

import org.junit.Rule;
import org.junit.Test;

/**
 * Starts the system with all spec scripts running, and
 * loads all scripts in the system to test them
 * 
 * @author jason
 *
 */
@Subscriber
public class SystemScriptsTest {

	@Rule
	public JibbrJabbrTestServer server =
		new JibbrJabbrTestServer(App.configuration)        // we use the configuration because that loads all the core configuration scripts
			.runAllSpecs()                                 // this defaults to off.  that might not survive now!
			.injectInstance(this)                          // well, sure
			.withModule(new JJModule() {
				@Override
				protected void configure() {
					addAPIModulePath("/jj/testing/specs"); // and put the specs on the path
				}
			});
	
	@Inject
	ResourceLoader resourceLoader;
	
	final int total = 3; // well, it's manual but maybe the phaser does what i need?
	final CountDownLatch testCountLatch = new CountDownLatch(total);
	final AtomicInteger successCount = new AtomicInteger();
	final AtomicInteger failureCount = new AtomicInteger();
	
	@Listener
	void testPassed(JasmineTestSuccess success) {
		successCount.incrementAndGet();
		testCountLatch.countDown();
	}
	
	@Listener
	void testFailed(JasmineTestFailure failure) {
		failureCount.incrementAndGet();
		testCountLatch.countDown();
	}
	
	private void load(String name) {
		resourceLoader.loadResource(ScriptResource.class, APIModules, name);
	}
	
	@Test
	public void test() throws Exception {
		
		// load everything we care about here!
		load("broadcast.js");
		
		// could take a while!
		assertTrue("timed out", testCountLatch.await(total * 250, MILLISECONDS));
		assertThat(failureCount.get() + " failed", failureCount.get(), is(0));
		assertThat(successCount.get(), is(total)); // just for certainty?
		
	}
	
}
