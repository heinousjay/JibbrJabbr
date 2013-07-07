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
import jj.MockJJExecutors;
import jj.MockJJExecutors.ThreadType;
import jj.resource.ResourceFinder;
import jj.resource.ScriptResource;
import jj.resource.ScriptResourceType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptBundleHelperTest {
	
	@Mock ResourceFinder finder;
	ScriptBundles scriptBundles;
	@Mock ScriptBundleCreator creator;
	MockJJExecutors executors;
	@Mock ScriptResource scriptResource;
	@Mock ScriptResource scriptResource1;
	@Mock ScriptResource scriptResource2;
	@Mock ModuleScriptBundle moduleScriptBundle;
	@Mock AssociatedScriptBundle associatedScriptBundle;
	
	@Before
	public void before() {
		scriptBundles = new ScriptBundles();
		executors = new MockJJExecutors();
	}

	@Test
	public void testFindModuleScriptBundle() {
		String baseName = "index";
		String moduleIdentifier = "helpers/linkify";
		
		given(finder.findResource(ScriptResource.class, moduleIdentifier, ScriptResourceType.Module)).willReturn(scriptResource);
		given(creator.createScriptBundle(scriptResource, moduleIdentifier, baseName)).willReturn(moduleScriptBundle);
		
		ScriptBundleHelper underTest = new ScriptBundleHelper(finder, scriptBundles, creator, executors);
		
		ModuleScriptBundle result = underTest.scriptBundleFor(baseName, moduleIdentifier);
		
		assertThat(result, is(moduleScriptBundle));
	}

	@Test
	public void testFindModuleScriptBundle2() {
		String baseName = "chat/index";
		String moduleIdentifier = "helpers/linkify";
		
		given(finder.findResource(ScriptResource.class, "chat/helpers/linkify", ScriptResourceType.Module)).willReturn(scriptResource);
		given(creator.createScriptBundle(scriptResource, moduleIdentifier, baseName)).willReturn(moduleScriptBundle);
		
		ScriptBundleHelper underTest = new ScriptBundleHelper(finder, scriptBundles, creator, executors);
		
		ModuleScriptBundle result = underTest.scriptBundleFor(baseName, moduleIdentifier);
		
		assertThat(result, is(moduleScriptBundle));
	}
	
	@Test
	public void testFindAssociatedScriptBundle() {
		String baseName = "index";
		
		given(finder.findResource(ScriptResource.class, "index", ScriptResourceType.Client)).willReturn(scriptResource1);
		given(finder.findResource(ScriptResource.class, "index", ScriptResourceType.Shared)).willReturn(null);
		given(finder.findResource(ScriptResource.class, "index", ScriptResourceType.Server)).willReturn(scriptResource2);
		
		given(creator.createScriptBundle(scriptResource1, null, scriptResource2, baseName)).willReturn(associatedScriptBundle);
		
		executors.addThreadTypes(ThreadType.ScriptThread, 10);
		
		ScriptBundleHelper underTest = new ScriptBundleHelper(finder, scriptBundles, creator, executors);
		
		AssociatedScriptBundle result = underTest.scriptBundleFor(baseName);
		
		assertThat(result, is(associatedScriptBundle));
	}
}
