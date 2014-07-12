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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.App;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.jasmine.JasmineTestFailure;
import jj.jasmine.JasmineTestSuccess;
import jj.resource.ResourceLoader;

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
		new JibbrJabbrTestServer(App.configuration).runAllSpecs().injectInstance(this);
	
	@Inject
	ResourceLoader resourceLoader;
	
	final int total = 1;
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
	
	@Test
	public void test() throws Exception {
		
		// load everything we care about here!
		
		// could take a while!
		assertTrue(testCountLatch.await(10, SECONDS));
		assertThat(successCount.get(), is(total));
		assertThat(failureCount.get(), is(0));
		
	}
	
}
