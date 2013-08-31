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

import java.nio.file.Paths;

import jj.engine.EngineAPI;
import jj.resource.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptBundleCreatorTest {
	
	String baseName;
	
	@Mock ScriptResource moduleScriptResource;
	
	RhinoContextMaker contextMaker;
	
	@Mock EngineAPI engineAPI;
	
	ScriptBundleCreator scriptBundleCreator;
	
	ScriptableObject global;
	
	@Before
	public void before() {
		
		baseName = "chat/index";
		
		contextMaker = new RealRhinoContextMaker();
		
		global = contextMaker.generalScope();
		
		given(engineAPI.global()).willReturn(global);
		
		scriptBundleCreator = new ScriptBundleCreator(engineAPI, contextMaker);
	}

	@Test
	public void testModuleBundleCreation() {
		
		// given
		final String moduleIdentifier = "helpers/messages";
		
		// doesn't really matter what our path is
		
		// for now, this serves as a basic test of the module API
		given(moduleScriptResource.script()).willReturn("var id = module.id; exports.hi = function() { return id };");
		given(moduleScriptResource.path()).willReturn(Paths.get("/"));
		
		// when
		ModuleScriptBundle result = scriptBundleCreator.createScriptBundle(moduleScriptResource, moduleIdentifier, baseName);
		
		// then
		assertThat(result, is(notNullValue()));
		assertThat(result.exports(), is(notNullValue()));
		
		// when
		// we execute the script - seemingly not related to this test
		// but it verifies we put the exports and module objects in
		// the right place, which is a responsibility of the creator
		try (RhinoContext cx = new RealRhinoContextMaker().context()) {
			cx.executeScriptWithContinuations(result.script(), result.scope());
		}
		
		// then
		Object hi = result.exports().get("hi", result.exports());
		assertTrue(hi instanceof Function);
		Function hiFunc = (Function)hi;
		Object moduleId;
		
		try (RhinoContext cx = new RealRhinoContextMaker().context()) {
			moduleId = cx.callFunction(hiFunc, global, global);
		}
		
		assertEquals(moduleIdentifier, moduleId);
	}
}
