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
import static jj.application.AppLocation.*;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

import javax.inject.Inject;

import jj.App;
import jj.ServerRoot;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.FileResource;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.script.module.ScriptResource;
import jj.testing.JibbrJabbrTestServer;

import jj.testing.Latch;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
@Subscriber
public class JasmineIntegrationTest {
	
	@Rule
	public JibbrJabbrTestServer jj = 
		new JibbrJabbrTestServer(ServerRoot.one, App.jasmine)
			.withFileWatcher()
			.injectInstance(this);
	
	@Inject ResourceLoader resourceLoader;
	@Inject ResourceFinder resourceFinder;
	
	Latch latch;
	
	JasmineTestSuccess success;
	JasmineTestFailure failure;
	
	@Listener
	void on(JasmineTestSuccess success) {
		this.success = success;
		latch.countDown();
	}

	@Listener
	void on(JasmineTestFailure failure) {
		this.failure = failure;
		latch.countDown();
	}

	@Listener
	void on(JasmineTestError error) {
		latch.countDown();
	}

	@Test
	public void test() throws Exception {
		
		latch = new Latch(2);
		
		// loading a script resource triggers the jasmine run
		resourceLoader.loadResource(ScriptResource.class, Private, "jasmine-int-test.js");
		resourceLoader.loadResource(ScriptResource.class, Private, "jasmine-int-test-failures.js");
		
		// takes about 1 second locally
		// maybe externalize timeouts?  or produce a factor on travis?
		assertTrue("timed out", latch.await(3, SECONDS));
		
		// make sure we got notified as expected
		assertNotNull(success);
		assertNotNull(failure);
		
		// contents of test results are verified in the unit tests
		success = null;
		failure = null;
		latch = new Latch(2);
		
		touch(resourceFinder.findResource(ScriptResource.class, Private, "jasmine-int-test.js"));
		touch(resourceFinder.findResource(ScriptResource.class, Private, "jasmine-int-test-failures.js"));
		
		assertTrue("timed out", latch.await(3, SECONDS));
		
		// make sure they ran again
		assertNotNull(success);
		assertNotNull(failure);
	}
	
	// also need a delete test!
	private void touch(FileResource<?> resource) throws Exception {
		FileTime originalFileTime = Files.getLastModifiedTime(resource.path());
		FileTime newFileTime;
		do {
			newFileTime = FileTime.fromMillis(System.currentTimeMillis());
		} while (newFileTime.compareTo(originalFileTime) < 1);
		
		Files.setLastModifiedTime(resource.path(), newFileTime);
	}
}
