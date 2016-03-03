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

import static org.mockito.Mockito.mock;
import io.netty.channel.Channel;

import org.junit.runner.Description;

import jj.CoreModule;
import jj.JJModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the server kernel without the http server,
 * for testing.
 * 
 * @author jason
 *
 */
class TestModule extends JJModule {
	
	private final JibbrJabbrTestServer testServer;
	private final String[] args;
	private final Description description;
	private final boolean withHttpServer;
	
	TestModule(
		JibbrJabbrTestServer testServer,
		String[] args,
		Description description,
		boolean withHttpServer
	) {
		this.testServer = testServer;
		this.args = args;
		this.description = description;
		this.withHttpServer = withHttpServer;
	}

	@Override
	protected void configure() {
		
		bind(JibbrJabbrTestServer.class).toInstance(testServer);
		
		bind(Description.class).toInstance(description);

		bind(Logger.class).annotatedWith(TestRunnerLogger.class).toInstance(LoggerFactory.getLogger(TestRunnerLogger.NAME));
		
		if (!withHttpServer) {
			bind(Channel.class).toInstance(mock(Channel.class));
		}
		
		install(new CoreModule(args));
		
		if (testServer.modules != null) {
			testServer.modules.forEach(this::install);
		}
	}
}
