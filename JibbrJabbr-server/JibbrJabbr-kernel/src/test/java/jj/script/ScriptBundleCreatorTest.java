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
package jj.script;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.JJ;
import jj.hostapi.RhinoObjectCreator;
import jj.resource.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptBundleCreatorTest {
	
	String baseName;
	
	@Mock ScriptResource moduleScriptResource;
	
	@Mock RhinoObjectCreator rhinoObjectCreator;
	
	ScriptBundleCreator scriptBundleCreator;
	
	ScriptableObject global;
	
	@Before
	public void before() {
		
		baseName = "index";
		
		global = Context.enter().initStandardObjects();
		Context.exit();
		
		
		given(rhinoObjectCreator.context()).will(new Answer<Context>() {

			@Override
			public Context answer(InvocationOnMock invocation) throws Throwable {
				return Context.enter();
			}
		});
		
		given(rhinoObjectCreator.global()).willReturn(global);
		
		scriptBundleCreator = new ScriptBundleCreator(rhinoObjectCreator);
	}

	@Test
	public void testModuleBundleCreation() {
		
		// given
		final String moduleIdentifier = "messages";
		
		// doesn't really matter what our path is
		Path path = Paths.get(JJ.uri(ScriptBundleCreatorTest.class));
		
		// for now, this serves as a basic test of the module API
		given(moduleScriptResource.script()).willReturn("var id = module.id; exports.hi = function() { return id };");
		given(moduleScriptResource.path()).willReturn(path);
		
		// when
		ModuleScriptBundle result = scriptBundleCreator.createScriptBundle(moduleScriptResource, moduleIdentifier, baseName);
		
		// then
		assertThat(result, is(notNullValue()));
		assertThat(result.exports(), is(notNullValue()));
		
		// when
		// we execute the script - seemingly not related to this test
		// but it verifies we put the exports and module objects in
		// the right place, which is a responsibility of the creator
		Context cx = Context.enter();
		try {
			result.script().exec(cx, result.scope());
		} finally {
			Context.exit();
		}
		
		// then
		Object hi = result.exports().get("hi", result.exports());
		assertTrue(hi instanceof Callable);
		Callable hiFunc = (Callable)hi;
		Object moduleId;
		cx = Context.enter();
		try {
			moduleId = hiFunc.call(cx, global, global, new Object[0]);
		} finally {
			Context.exit();
		}
		
		assertEquals(moduleIdentifier, moduleId);
	}
}
