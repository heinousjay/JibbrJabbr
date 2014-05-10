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
package jj.configuration.resolution;

import static jj.configuration.resolution.AppLocation.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.configuration.Arguments;
import jj.configuration.resolution.APIModules;
import jj.configuration.resolution.Application;
import jj.configuration.resolution.Assets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

	@Mock private Arguments arguments;
	@Mock private Assets assets;
	@Mock private APIModules apiModules;
	@InjectMocks Application app;
	
	@Test
	public void test() {
		
		// virtual resource have no application path
		Path path = app.resolvePath(Virtual, "");
		assertThat(path, is(nullValue()));
		
		// asset resources get delegated to Assets
		app.resolvePath(Assets, "");
		verify(assets).path("");
		
		// api module resources get delegated to APIModules
		app.resolvePath(APIModules, "");
		verify(apiModules).path("");
		
		// otherwise we're doing something worth looking at
		
		// first, default config, no argument for app path
		assertThat(app.resolvePath(Base, "config.js"), is(Paths.get("app/config.js")));
		assertThat(app.resolvePath(Public, "index.html"), is(Paths.get("app/public/index.html")));
		assertThat(app.resolvePath(Public, "deep/index.html"), is(Paths.get("app/public/deep/index.html")));
		assertThat(app.resolvePath(Public, "deep/and/deeper/index.html"), is(Paths.get("app/public/deep/and/deeper/index.html")));
		assertThat(app.resolvePath(Private, "index.js"), is(Paths.get("app/private/index.js")));
		assertThat(app.resolvePath(PublicSpecs, "index.js"), is(Paths.get("app/public-specs/index.js")));
		assertThat(app.resolvePath(PrivateSpecs, "index.js"), is(Paths.get("app/private-specs/index.js")));
		
		
		// and then with an argument
		given(arguments.get("app", Path.class)).willReturn(Paths.get("other"));
		
		assertThat(app.resolvePath(Base, "config.js"), is(Paths.get("other/config.js")));
		assertThat(app.resolvePath(Public, "index.html"), is(Paths.get("other/public/index.html")));
		assertThat(app.resolvePath(Private, "index.js"), is(Paths.get("other/private/index.js")));
		assertThat(app.resolvePath(PublicSpecs, "index.js"), is(Paths.get("other/public-specs/index.js")));
		assertThat(app.resolvePath(PublicSpecs, "deep/index.js"), is(Paths.get("other/public-specs/deep/index.js")));
		assertThat(app.resolvePath(PublicSpecs, "deep/and/deeper/index.js"), is(Paths.get("other/public-specs/deep/and/deeper/index.js")));
		assertThat(app.resolvePath(PrivateSpecs, "index.js"), is(Paths.get("other/private-specs/index.js")));
	}

}
