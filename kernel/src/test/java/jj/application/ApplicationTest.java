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
package jj.application;

import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.nio.file.Path;

import jj.application.Application;
import jj.configuration.Arguments;
import jj.server.Server;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

	@Mock private Arguments arguments;
	@Mock private Server server;
	
	Application app;
	
	@Before
	public void before() {
		given(server.resolvePath(Root, "app")).willReturn(jj.Base.path);
		given(arguments.get("app", Path.class, jj.Base.path)).willReturn(jj.Base.path);
		app = new Application(arguments, server);
	}
	
	@Test
	public void test() {
		// first, default config, no argument for app path
		Path base = jj.Base.path;
		
		assertThat(app.resolvePath(Base, "config.js"), is(base.resolve("config.js")));
		assertThat(app.resolvePath(Public, "index.html"), is(base.resolve("public/index.html")));
		assertThat(app.resolvePath(Public, "deep/index.html"), is(base.resolve("public/deep/index.html")));
		assertThat(app.resolvePath(Public, "deep/and/deeper/index.html"), is(base.resolve("public/deep/and/deeper/index.html")));
		assertThat(app.resolvePath(Private, "index.js"), is(base.resolve("private/index.js")));
		assertThat(app.resolvePath(Specs, "index.js"), is(base.resolve("specs/index.js")));
	}

}
