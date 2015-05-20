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
package jj.script.module;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.application.AppLocation;
import jj.application.MockApplication;
import jj.resource.MockAbstractResourceDependencies;
import jj.script.MockRhinoContextProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Script;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptResourceTest {
	
	private static final String NAME =  "test.js";
	
	MockAbstractResourceDependencies dependencies;
	MockRhinoContextProvider contextProvider;
	Path rootPath;
	
	@Mock Script script;
	
	MockApplication app;
	
	@Before
	public void before() throws Exception {
		dependencies = new MockAbstractResourceDependencies(AppLocation.Base, NAME);
		contextProvider = new MockRhinoContextProvider();
		rootPath = Paths.get(ScriptResourceTest.class.getResource("/jj/script/module/test.js").toURI()).getParent();
		app = new MockApplication(rootPath);
	}

	@Test
	public void test() throws Exception {
		
		String contents = new String(Files.readAllBytes(rootPath.resolve(NAME)), UTF_8);
		
		given(contextProvider.context.compileString(contents, NAME)).willReturn(script);
		
		ScriptResource resource = new ScriptResource(dependencies, rootPath.resolve(NAME), contextProvider, app);
		
		assertThat(resource.script(), is(script));
	}

}
