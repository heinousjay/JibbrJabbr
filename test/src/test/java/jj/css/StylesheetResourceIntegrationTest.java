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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static jj.server.ServerLocation.Virtual;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;

import jj.App;
import jj.ServerRoot;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoaded;
import jj.resource.ResourceLoader;
import jj.testing.JibbrJabbrTestServer;

import jj.testing.Latch;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
@Subscriber
public class StylesheetResourceIntegrationTest {
	
	@Rule
	public JibbrJabbrTestServer testServer = new JibbrJabbrTestServer(ServerRoot.one, App.css).injectInstance(this);
	
	@Inject ResourceLoader resourceLoader;
	@Inject ResourceFinder resourceFinder;
	
	StylesheetResource stylesheet;
	
	Latch latch;
	
	@Listener
	void on(ResourceLoaded event) {
		if (event.type() == StylesheetResource.class) {
			stylesheet = resourceFinder.findResource(StylesheetResource.class, event.base(), event.name());
			latch.countDown();
		}
	}
	
	@Before
	public void before() {
		stylesheet = null;
		latch = new Latch(1);
	}

	@Test
	public void testLess() throws Exception {
		resourceLoader.loadResource(StylesheetResource.class, Virtual, "less.css");
		
		assertTrue("timed out", latch.await(2, SECONDS));
		
		String lessOutput = stylesheet.bytes().toString(UTF_8);
		String expectedOutput = new String(Files.readAllBytes(Paths.get(App.css + "/test.css.output")), UTF_8);
		
		assertThat(lessOutput, is(expectedOutput));
	}
	
	
	@Test
	public void testCss() throws Exception {
		resourceLoader.loadResource(StylesheetResource.class, Virtual, "test.css");
		
		assertTrue("timed out", latch.await(2, SECONDS));
		
		String cssOutput = stylesheet.bytes().toString(UTF_8);
		String expectedOutput = new String(Files.readAllBytes(Paths.get(App.css + "/test.css.output")), UTF_8);
		
		assertThat(cssOutput, is(expectedOutput));
	}
	
	@Test
	public void testReplacements() throws Exception {
		latch = new Latch(2); // we want two!
		resourceLoader.loadResource(StylesheetResource.class, Virtual, "replacement.css");
		
		assertTrue("timed out", latch.await(2, SECONDS));
		
		String cssOutput = stylesheet.bytes().toString(UTF_8);
		String expectedOutput = new String(Files.readAllBytes(Paths.get(App.css + "/replacement.css.output")), UTF_8);
		
		assertThat(cssOutput, is(expectedOutput));
	}

}
