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
package jj.resource.document;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Paths;

import jj.engine.EngineAPI;
import jj.event.Publisher;
import jj.execution.ExecutionEvent;
import jj.resource.NoSuchResourceException;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceNotViableException;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.script.MockRhinoContextProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@RunWith(MockitoJUnitRunner.class)
public class DocumentScriptEnvironmentTest {

	@Mock HtmlResource html;
	@Mock ScriptResource script;
	@Mock ResourceFinder resourceFinder;
	@Mock Publisher publisher;
	@Mock EngineAPI api;
	@Mock ScriptableObject local;
	@Mock ResourceCacheKey cacheKey;
	@Mock ScriptCompiler scriptCompiler;
	MockRhinoContextProvider contextMaker;
	
	@Captor ArgumentCaptor<ExecutionEvent> eventCaptor;

	@Before
	public void before() throws Exception {
		contextMaker = new MockRhinoContextProvider();
		given(contextMaker.context.newObject(any(Scriptable.class))).willReturn(local);
		
		given(script.path()).willReturn(Paths.get("/"));
	}
	
	private void givenAnHtmlResource(String baseName) throws Exception {
		given(resourceFinder.loadResource(HtmlResource.class, HtmlResourceCreator.resourceName(baseName))).willReturn(html);
	}

	private void givenAClientScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Client.suffix(baseName))).willReturn(script);
	}

	private void givenASharedScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Shared.suffix(baseName))).willReturn(script);
	}

	private void givenAServerScript(String baseName) throws Exception {
		given(resourceFinder.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName))).willReturn(script);
	}
	
	@Test
	public void testThrowsNotFound() throws Exception {
		
		String baseName = "index";
		
		try {
			new DocumentScriptEnvironment(cacheKey, baseName, resourceFinder, contextMaker, api, publisher, scriptCompiler);
			fail();
		} catch (NoSuchResourceException nsre) {
			assertThat(nsre.getMessage(), is(baseName + "-" + baseName + ".html"));
		}
	}

	@Test
	public void testCompilationFailureHandledCorrectly() throws Exception {
		
		String name = "broken";
		
		givenAnHtmlResource(name);
		givenASharedScript(name);
		givenAServerScript(name);
		
		RuntimeException re = new RuntimeException();
		
		given(scriptCompiler.compile(local, null, script, script)).willThrow(re);
		
		try {
			new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher, scriptCompiler);
			fail();
		} catch (ResourceNotViableException rnve) {
			assertThat(rnve.getMessage(), is(name));
			assertThat(rnve.getCause(), is(sameInstance((Throwable)re)));
		}
	}
	
	// i know this looks like a private method is being tested... and sure, it is... but the
	// job this class does needs to be rigorously specified to ensure the system runs properly
	@Test
	public void testLocalScopeSetup() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAServerScript(name);
		ScriptableObject scope = mock(ScriptableObject.class);
		given(api.global()).willReturn(scope);
		
		new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher, scriptCompiler);
		
		verify(local).setPrototype(scope);
		verify(local).setParentScope(null);
		
		// local is return as all new object results.  luckily nothing overlaps
		verify(local).defineProperty("module", local, ScriptableObject.CONST);
		verify(local).defineProperty("id", name, ScriptableObject.CONST);
		verify(local).defineProperty(eq("require"), isNull(), eq(ScriptableObject.CONST));
		
	}
	
	@Test
	public void testAllTogether() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		given(script.script()).willReturn("");
		givenASharedScript(name);
		givenAServerScript(name);
		
		DocumentScriptEnvironment result = new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher, scriptCompiler);
		
		verify(html).addDependent(result);
		verify(script, times(3)).addDependent(result);
		
		assertThat(result.sha1().length(), is(40));
		assertThat(result.uri(), is("/" + result.sha1() + "/" + name));
		assertThat(result.socketUri(), is("/" + result.sha1() + "/" + name + ".socket"));
	}
	
	@Test
	public void testHTMLWithNoServerScript() throws Exception {
		
		String name = "index";
		
		givenAnHtmlResource(name);
		givenAClientScript(name);
		givenASharedScript(name);
		
		DocumentScriptEnvironment result = new DocumentScriptEnvironment(cacheKey, name, resourceFinder, contextMaker, api, publisher, scriptCompiler);
		
		assertThat(result.sha1().length(), is(40));
		assertThat(result.uri(), is("/" + result.sha1() + "/" + name));
		assertThat(result.socketUri(), is(nullValue()));
		
		assertThat(result.scope(), is(nullValue()));
		assertThat(result.script(), is(nullValue()));
	}
}
