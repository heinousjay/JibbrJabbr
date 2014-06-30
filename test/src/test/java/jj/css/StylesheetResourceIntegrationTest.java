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
package jj.css;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.configuration.resolution.AppLocation.Base;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jj.App;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoaded;
import jj.resource.ResourceLoader;
import jj.testing.JibbrJabbrTestServer;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
@Subscriber
public class StylesheetResourceIntegrationTest {
	
	@Rule
	public JibbrJabbrTestServer testServer = new JibbrJabbrTestServer(App.css).injectInstance(this);
	
	@Inject ResourceLoader resourceLoader;
	@Inject ResourceFinder resourceFinder;
	
	StylesheetResource stylesheet;
	
	CountDownLatch latch = new CountDownLatch(1);
	
	@Listener
	void resourceLoaded(ResourceLoaded event) {
		if (event.resourceClass == StylesheetResource.class) {
			stylesheet = resourceFinder.findResource(StylesheetResource.class, event.base, event.name);
			latch.countDown();
		}
	}

	// this test fails in eclipse. there's something funky about the classpath i need to figure out
	@Test
	public void testLess() throws Exception {
		resourceLoader.loadResource(StylesheetResource.class, Base, "less.css");
		
		assertTrue("timed out", latch.await(2, SECONDS));
		
		String lessOutput = stylesheet.bytes().toString(UTF_8);
		String expectedOutput = new String(Files.readAllBytes(Paths.get(App.css + "/test.css")), UTF_8);
		
		assertThat(lessOutput, is(expectedOutput));
	}

}
