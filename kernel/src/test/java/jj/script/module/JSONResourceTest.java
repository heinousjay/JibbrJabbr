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

import static jj.configuration.resolution.AppLocation.Base;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.resource.MockAbstractResourceDependencies;
import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
public class JSONResourceTest {
	
	RealRhinoContextProvider contextProvider;
	ScriptableObject global;
	
	Path rootPath;
	
	@Before
	public void before() throws Exception {
		contextProvider = new RealRhinoContextProvider();
		
		try (RhinoContext context = contextProvider.get()) {
			global = context.initStandardObjects();
		}
		
		rootPath = Paths.get(ScriptResourceTest.class.getResource("/jj/script/module/test1.json").toURI()).getParent();
	}

	@Test
	public void test() throws Exception {
		
		JSONResource r = new JSONResource(new MockAbstractResourceDependencies(Base, "test.json"), rootPath.resolve("test1.json"), global, contextProvider);
		
		assertThat(r.contents(), is("this is a string"));
		
		r = new JSONResource(new MockAbstractResourceDependencies(Base, "test.json"), rootPath.resolve("test2.json"), global, contextProvider);
		
		assertThat(r.contents(), is(23123));
		
		r = new JSONResource(new MockAbstractResourceDependencies(Base, "test.json"), rootPath.resolve("test3.json"), global, contextProvider);
		
		assertThat(r.contents(), is(instanceOf(Scriptable.class)));
		Scriptable c = (Scriptable)r.contents();
		assertThat(c.get("whatever", c), is(not(Scriptable.NOT_FOUND)));
	}

}
