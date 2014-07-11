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
import static jj.configuration.resolution.AppLocation.*;
import static jj.resource.ResourceEventMaker.makeResourceLoaded;
import static jj.resource.DependentsHelper.verifyDependentSetup;

import jj.resource.NoSuchResourceException;
import jj.resource.ResourceFinder;
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
	private ScriptResource fakeResource(ScriptResource sr) {
		given(sr.sha1()).willReturn("da39a3ee5e6b4b0d3255bfef95601890afd8070" + (++count));		
		return sr;
	}
	
	@Before
	public void before() {
		count = 0;
		dependencies = new MockAbstractScriptEnvironmentDependencies();
		given(dependencies.rhinoContextProvider().context.newObject(global)).willReturn(global);
		
		given(jasmine.script()).willReturn(jasmineScript);
		given(jasmineBoot.script()).willReturn(jasmineBootScript);
		
		fakeResource(jasmine);
		given(resourceFinder.loadResource(ScriptResource.class, Assets, "jasmine.js")).willReturn(jasmine);
		
		fakeResource(jasmineBoot);
		given(resourceFinder.loadResource(ScriptResource.class, Assets, "jasmine-boot.js")).willReturn(jasmineBoot);
		
		fakeResource(jasmineRun);
		given(resourceFinder.loadResource(ScriptResource.class, Assets, "jasmine-run.js")).willReturn(jasmineRun);
		
		given(target.base()).willReturn(Base);
		given(target.name()).willReturn(targetName);
		
		fakeResource(target);
		given(resourceFinder.loadResource(ScriptResource.class, Base, targetName)).willReturn(target);
		
		given(spec.base()).willReturn(Base);
		given(spec.name()).willReturn(specName);
		
	}
	
	@Test(expected = NoSuchResourceException.class)
	public void testNotFound() {
		new JasmineScriptEnvironment(dependencies, specName, global, resourceFinder, makeResourceLoaded(target));
	}

	@Test
	public void testFound() {
		
		fakeResource(spec);
		given(resourceFinder.loadResource(ScriptResource.class, Base, specName)).willReturn(spec);
		
		JasmineScriptEnvironment jse = new JasmineScriptEnvironment(dependencies, specName, global, resourceFinder, makeResourceLoaded(target));
		
		assertThat(jse.name(), is(specName));
		assertThat(jse.script(), is(jasmineBootScript));
		
		verifyDependentSetup(jasmine, jse);
		verifyDependentSetup(jasmineBoot, jse);
		verifyDependentSetup(jasmineRun, jse);
		verifyDependentSetup(target, jse);
		verifyDependentSetup(spec, jse);
		
		// make sure we ran our setup (at least the part that matters internally)
		verify(dependencies.rhinoContextProvider().context).executeScript(jasmineScript, global);
		
		// TODO VERIFY THE SCOPE!
		// need some test tools for this
		// and need to tweak down the scope anyway.  it's not right, yet.
		// inject needs to be renamed
		// require needs to be renamed
		
		// verify the sha is build from all our script buddies.  kind of goofy but correct
		// this is sort of future-prep but this might become a target for socket connections at
		// some point, to deliver test run results to the browser.  maybe
		assertThat(jse.sha1(), is("2aa82b3b93e7e4ed4cb7b8aa7c350e84016a0014"));
		assertThat(jse.uri(), is("/2aa82b3b93e7e4ed4cb7b8aa7c350e84016a0014/" + specName));
	}
	
}