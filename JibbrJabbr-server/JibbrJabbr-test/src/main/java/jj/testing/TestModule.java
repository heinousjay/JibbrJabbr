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

import jj.CoreModule;
import jj.JJModule;

/**
 * @author jason
 *
 */
class TestModule extends JJModule {
	
	private final String appPath;
	private final Description description;
	
	TestModule(final String appPath, final Description description) {
		this.appPath = appPath;
		this.description = description;
	}

	@Override
	protected void configure() {
		
		addServerListenerBinding().to(TestListener.class);
		
		bind(Description.class).toInstance(description);
		
		bind(FullHttpRequest.class).toInstance(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"));
		
		bind(Channel.class).toInstance(mock(Channel.class));
		
		install(new CoreModule(new String[]{appPath}, true));
	}
}
