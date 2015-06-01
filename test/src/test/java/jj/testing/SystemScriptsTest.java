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
import static jj.server.ServerLocation.APIModules;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import jj.App;
import jj.JJ;
import jj.ServerRoot;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.jasmine.JasmineTestError;
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
	
	private static final String SPEC_PATH = "/jj/testing/specs";
	private static final int total;
	
	private static int count(Path in) throws Exception {

		// the cast is ugly but if i ever end up with more than 2.1 billion
		// specs i guess addressing this will be a pleasure haha
		
		return (int)Files.list(in).filter(path -> { return path.toString().endsWith(".js"); }).count();
	}
	
	static {
		try {
			
			
			URI specUri = App.class.getResource(SPEC_PATH).toURI();
			Path jarPath = JJ.jarPath(specUri);
			if (jarPath != null) {
				try (FileSystem jar = FileSystems.newFileSystem(jarPath, null)) {
					total = count(jar.getPath(SPEC_PATH));
				}
			} else {
				total = count(Paths.get(specUri));
			}
			
			//System.out.println(total + " specs found in " + specUri);
			
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	

	@Rule
	public JibbrJabbrTestServer server =
		new JibbrJabbrTestServer(ServerRoot.one, App.configuration) // we use the configuration because that loads all the core configuration scripts
			.runAllSpecs()                          // this way, it runs specs regardless
			.injectInstance(this);                  // well, sure

	
	@Inject
	ResourceLoader resourceLoader;
	
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
	
	@Listener
	void testErrored(JasmineTestError error) {
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
		load("configuration-support.js"); // loaded by configuration
		load("console.js");
		load("env.js");
		load("globalize.js");
		load("local-storage.js");
		load("resource-properties.js");
		load("server-events.js");
		load("system-properties.js");
		load("uri-routing-configuration.js"); // loaded by configuration
		
		assertTrue("timed out", testCountLatch.await(total * 250, MILLISECONDS));
		assertThat(failureCount.get() + " failed", failureCount.get(), is(0));
		assertThat(successCount.get(), is(total)); // just for certainty?
	}
	
}
