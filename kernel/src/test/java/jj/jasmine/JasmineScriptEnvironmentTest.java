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
package jj.jasmine;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;
import static jj.resource.ResourceEventMaker.makeResourceLoaded;
import static jj.resource.DependentsHelper.verifyDependentSetup;

import jj.resource.*;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.module.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JasmineScriptEnvironmentTest {

	@Mock PathResolver pathResolver;
	@Mock ResourceFinder resourceFinder;
	@Mock ScriptableObject global;
	
	@Mock ScriptResource jasmine;
	@Mock Script jasmineScript;
	
	@Mock ScriptResource jasmineBoot;
	@Mock Script jasmineBootScript;
	
	@Mock ScriptResource jasmineRun;
	
	String targetName = "name.js";
	@Mock ScriptResource target;
	
	String specName = "name-spec.js";
	@Mock ScriptResource spec;
	
	MockAbstractScriptEnvironmentDependencies dependencies;
	
	int count = 0;
	@SuppressWarnings("unchecked")
	private ScriptResource fakeResource(ScriptResource sr) {
		given(sr.sha1()).willReturn("da39a3ee5e6b4b0d3255bfef95601890afd8070" + (++count));
		given((ResourceIdentifier<ScriptResource, Void>)sr.identifier()).willReturn(
			new MockResourceIdentifierMaker().make(ScriptResource.class, AppBase, "script" + count)
		);
		return sr;
	}
	
	@Before
	public void before() {
		given(target.base()).willReturn(AppBase);
		given(target.name()).willReturn(targetName);

		fakeResource(target);

		count = 0;
		dependencies = new MockAbstractScriptEnvironmentDependencies(JasmineScriptEnvironment.class, specName, makeResourceLoaded(target));
		given(dependencies.mockRhinoContextProvider().context.newObject(global)).willReturn(global);
		given(dependencies.mockRhinoContextProvider().context.newChainedScope(global)).willReturn(global);
		
		given(jasmine.script()).willReturn(jasmineScript);
		given(jasmineBoot.script()).willReturn(jasmineBootScript);
		
		fakeResource(jasmine);
		given(resourceFinder.loadResource(ScriptResource.class, Assets, "jasmine.js")).willReturn(jasmine);
		
		fakeResource(jasmineBoot);
		given(resourceFinder.loadResource(ScriptResource.class, Assets, "jasmine-boot.js")).willReturn(jasmineBoot);
		
		fakeResource(jasmineRun);
		given(resourceFinder.loadResource(ScriptResource.class, Assets, "jasmine-run.js")).willReturn(jasmineRun);

		given(resourceFinder.loadResource(ScriptResource.class, AppBase, targetName)).willReturn(target);
		
		given(spec.base()).willReturn(AppBase);
		given(spec.name()).willReturn(specName);
		
	}
	
	@Test(expected = NoSuchResourceException.class)
	public void testNotFound() {
		new JasmineScriptEnvironment(dependencies, global, resourceFinder, pathResolver, makeResourceLoaded(target));
	}

	@SuppressWarnings("unchecked")
	private void makeTargetFindable() {
		given(resourceFinder.loadResource((ResourceIdentifier<ScriptResource, Void>) target.identifier())).willReturn(target);
	}

	@Test
	public void testFound() {

		makeTargetFindable();
		
		fakeResource(spec);
		given(resourceFinder.loadResource(ScriptResource.class, AppBase, specName)).willReturn(spec);
		given(pathResolver.specLocationFor(AppBase)).willReturn(AppBase);
		
		JasmineScriptEnvironment jse = new JasmineScriptEnvironment(dependencies, global, resourceFinder, pathResolver, makeResourceLoaded(target));
		
		assertThat(jse.name(), is(specName));
		assertThat(jse.script(), is(jasmineBootScript));
		
		verifyDependentSetup(jasmine, jse);
		verifyDependentSetup(jasmineBoot, jse);
		verifyDependentSetup(jasmineRun, jse);
		verifyDependentSetup(target, jse);
		verifyDependentSetup(spec, jse);
		
		// make sure we ran our setup (at least the part that matters internally)
		verify(dependencies.mockRhinoContextProvider().context).executeScript(jasmineScript, global);
		
		// TODO VERIFY THE SCOPE!
		// need some test tools for this
		// and need to tweak down the scope anyway.  it's not right, yet.
		// inject needs to be renamed
		// require needs to be renamed
		
		// verify the sha is build from all our script buddies.  kind of goofy but correct
		// this is sort of future-prep but this might become a target for socket connections at
		// some point, to deliver test run results to the browser.  maybe
		assertThat(jse.sha1(), is("4a117d7e27db77e337750a4e380ef4587be12f40"));
	}
	
}