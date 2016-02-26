/*
 *    Copyright 2016 Jason Miller
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
package jj.configuration;

import jj.App;
import jj.ServerRoot;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.jasmine.JasmineConfiguration;
import jj.testing.JibbrJabbrTestServer;
import jj.testing.Latch;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Validates that a default configuration can
 * be loaded
 * @author Jason Miller
 */
@Subscriber
public class ConfigurationAddedTest {

	@Rule
	public JibbrJabbrTestServer app =
		new JibbrJabbrTestServer(ServerRoot.one, App.configuration2)
			.withFileWatcher()
			.injectInstance(this);

	@Inject
	JasmineConfiguration jasmineConfiguration;

	private final Latch latch = new Latch(2);

	@Listener
	void on(ConfigurationLoaded event) {
		latch.countDown();
	}

	@Listener
	void on(ConfigurationErrored event) {
		latch.countDown();
	}

	@Test
	public void test() throws Exception {
		assertThat(latch.getCount(), is(1L));
		assertThat(jasmineConfiguration.autorunSpecs(), is(false));

		try(BufferedWriter writer =
			Files.newBufferedWriter(
				App.configuration2.resolve("config.js"),
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE
			)
		) {
			writer.write("require('jj/jasmine-configuration').autorunSpecs(true);");
			writer.newLine();
			writer.flush();
		}

		// it can take a while to get noticed and load
		latch.await(2500, TimeUnit.MILLISECONDS);

		assertThat("did not load the new configuration", jasmineConfiguration.autorunSpecs(), is(true));
	}

	@After
	public void after() throws Exception {
		Files.delete(App.configuration2.resolve("config.js"));
	}
}
