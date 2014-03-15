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
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import jj.CoreModule;
import jj.JJModule;

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
	private final Statement base;
	private final Description description;
	private final boolean withHttpServer;
	
	TestModule(
		final JibbrJabbrTestServer testServer,
		final String[] args,
		final Statement base,
		final Description description,
		final boolean withHttpServer
	) {
		this.testServer = testServer;
		this.args = args;
		this.base = base;
		this.description = description;
		this.withHttpServer = withHttpServer;
	}

	@Override
	protected void configure() {
		
		addStartupListenerBinding().to(TestListener.class);
		
		bind(JibbrJabbrTestServer.class).toInstance(testServer);
		
		bind(Statement.class).toInstance(base);
		
		bind(Description.class).toInstance(description);
		
		bindLoggedEvents().annotatedWith(TestRunnerLogger.class).toLogger(TestRunnerLogger.NAME);
		
		if (!withHttpServer) {
			bind(FullHttpRequest.class).toInstance(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));
			bind(Channel.class).toInstance(mock(Channel.class));
		}
		
		install(new CoreModule(args));
	}
}
