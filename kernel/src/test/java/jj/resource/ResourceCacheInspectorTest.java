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
package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.List;

import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;

/**
 * the point here is to ensure we're outputting things in a basically correct
 * manner.  the actual output
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceCacheInspectorTest {
	
	@Mock ResourceCacheImpl resourceCacheImpl;
	RealRhinoContextProvider rhinoContextProvider;

	ResourceCacheInspector rca;
	
	// few different types (we don't get many in the core)
	// to validate that class information is passed through
	// correctly
	@Mock AbstractResource resource1;
	@Mock AbstractResource resource2;
	@Mock AbstractResource resource3;
	@Mock AbstractResource resource4;
	@Mock AbstractResource resource5;
	@Mock AbstractResource resource6;
	@Mock AbstractResource resource7;
	
	List<AbstractResource> resources;
	
	int total;
	
	@Before
	public void before() {
		
		// holy crap dependency lists
		// this literally might be better to do in the integration test.  it's getting ridiculous here
		given(resource1.dependents()).willReturn(Arrays.asList(resource2));
		given(resource2.dependents()).willReturn(Arrays.asList(resource1));
		given(resource3.dependents()).willReturn(Arrays.asList(resource4, resource5, resource6, resource7));
		
		resources = Arrays.asList(resource1, resource2, resource3, resource4, resource5, resource6, resource7);
		total = resources.size();
		given(resourceCacheImpl.allResources()).willReturn(resources);
		
		rhinoContextProvider = new RealRhinoContextProvider();
		
		try (RhinoContext context = rhinoContextProvider.get()) {
			rca = new ResourceCacheInspector(resourceCacheImpl, rhinoContextProvider, context.initStandardObjects());
		}
	}
	
	@Test
	public void test() {
		Scriptable nodesArray = rca.nodes();
		assertThat(nodesArray, is(notNullValue()));
		assertThat(nodesArray.get("length", nodesArray), is((double)total)); // js numbers are doubles!
		for (int i = 0; i < total; ++i) {
			Scriptable node = (Scriptable)nodesArray.get(i, nodesArray);
			assertThat(node, is(notNullValue()));
			assertThat(node.get("type", node), is(resources.get(i).getClass().getName()));
		}
		
		Scriptable linksArray = rca.links();
		assertThat(linksArray, is(notNullValue()));
		assertThat(linksArray.get("length", nodesArray), is(6D));
		
		Scriptable link = (Scriptable)linksArray.get(0, linksArray);
		assertThat(link.get("source", link), is(0));
		assertThat(link.get("target", link), is(1));
		
		link = (Scriptable)linksArray.get(1, linksArray);
		assertThat(link.get("source", link), is(1));
		assertThat(link.get("target", link), is(0));
		
		link = (Scriptable)linksArray.get(2, linksArray);
		assertThat(link.get("source", link), is(2));
		assertThat(link.get("target", link), is(3));
		
		link = (Scriptable)linksArray.get(3, linksArray);
		assertThat(link.get("source", link), is(2));
		assertThat(link.get("target", link), is(4));
		
		link = (Scriptable)linksArray.get(4, linksArray);
		assertThat(link.get("source", link), is(2));
		assertThat(link.get("target", link), is(5));
		
		link = (Scriptable)linksArray.get(5, linksArray);
		assertThat(link.get("source", link), is(2));
		assertThat(link.get("target", link), is(6));
	}

}
