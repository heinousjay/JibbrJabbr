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
package jj.css;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.resource.DependentsHelper.dependents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.application.MockApplication;
import jj.event.MockPublisher;
import jj.http.server.resource.StaticResource;
import jj.resource.*;
import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;
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
public class StylesheetResourceTest {
	
	private static final String NAME = "test.css";
	
	RealRhinoContextProvider contextProvider;
	ScriptableObject global;
	@Mock CssReferenceVersionProcessor processor;
	
	@Mock StaticResource cssResource;
	@Mock ScriptResource lessScriptResource;
	
	@Mock LessConfiguration lessConfiguration;
	
	Path cssPath;
	
	MockApplication app;
	
	@Before
	public void before() throws Exception {
		
		contextProvider = new RealRhinoContextProvider();
		
		cssPath = Paths.get(StylesheetResourceTest.class.getResource("/jj/css/test/test.css").toURI());
		app = new MockApplication(cssPath.getParent());
		given(cssResource.path()).willReturn(cssPath);
		given(cssResource.charset()).willReturn(UTF_8);
	}
	
	private StylesheetResource newStylesheet(MockAbstractResourceDependencies dependencies) {
		return new StylesheetResource(dependencies, contextProvider, global, processor, lessConfiguration, app);
	}

	@Test
	public void testNotFound() {
		MockAbstractResourceDependencies dependencies = new MockAbstractResourceDependencies(StylesheetResource.class, Virtual, NAME);
		try {
			newStylesheet(dependencies);
			fail();
		} catch (NoSuchResourceException nsre) {
			// yay
		}
		
		verify(dependencies.resourceFinder()).loadResource(StaticResource.class, Public, NAME);
		verify(dependencies.resourceFinder()).loadResource(LessResource.class, Private, "test.less");
	}
	
	@Test
	public void testCssFound() {
		MockAbstractResourceDependencies dependencies = new MockAbstractResourceDependencies(StylesheetResource.class, Virtual, NAME);
		
		given(dependencies.resourceFinder().loadResource(StaticResource.class, Public, NAME)).willReturn(cssResource);
		given(processor.fixUris(anyString(), isA(StylesheetResource.class))).willReturn("");
		
		StylesheetResource sr = newStylesheet(dependencies);
		
		verify(cssResource).addDependent(sr);
	}
	
	@Test
	public void testLessFound() throws Exception {
		ResourceFinder resourceFinder = mock(ResourceFinder.class);

		
		given(resourceFinder.loadResource(ScriptResource.class, Assets, StylesheetResource.LESS_SCRIPT)).willReturn(lessScriptResource);
		
		LessResource less1 = new LessResource(

			new MockAbstractResourceDependencies(ResourceIdentifierHelper.make(StylesheetResource.class, Private, "test.less"), resourceFinder),
			cssPath.resolveSibling("test.less")
		);
		given(resourceFinder.loadResource(LessResource.class, Private, "test.less")).willReturn(less1);
		given(resourceFinder.loadResource(LessResource.class, Public.and(Private), "test.less")).willReturn(less1);
		
		LessResource less2 = new LessResource(
			new MockAbstractResourceDependencies(ResourceIdentifierHelper.make(StylesheetResource.class, Private, "test2.less"), resourceFinder),
			cssPath.resolveSibling("test2.less")
		);

		given(resourceFinder.loadResource(LessResource.class, Public.and(Private), "test2.less")).willReturn(less2);
		
		given(processor.fixUris(anyString(), isA(StylesheetResource.class))).willAnswer(invocation -> invocation.getArguments()[0]);
		
		MockAbstractResourceDependencies dependencies = new MockAbstractResourceDependencies(
			ResourceIdentifierHelper.make(StylesheetResource.class, Virtual, NAME),
			resourceFinder
		);
		
		try (RhinoContext context = contextProvider.get().withoutContinuations()) {
			global = context.initStandardObjects();
			Script script = context.compileString(getLessScript(), "less script");
			given(lessScriptResource.script()).willReturn(script);
			
			
		// when
			StylesheetResource sr = newStylesheet(dependencies);
			
			
		// then
			verify(lessScriptResource).addDependent(sr);
			assertTrue(dependents(less1).contains(sr));
			assertTrue(dependents(less2).contains(sr));
			
			assertThat(sr.bytes().toString(UTF_8), is(new String(Files.readAllBytes(cssPath), UTF_8)));
		}
		
		MockPublisher publisher = dependencies.publisher();
		
		assertThat(publisher.events.size(), is(6));
		assertThat(publisher.events.get(0), is(instanceOf(StartingLessProcessing.class)));
		assertThat(publisher.events.get(1), is(instanceOf(LoadingLessResource.class)));
		assertThat(publisher.events.get(2), is(instanceOf(LoadingLessResource.class)));
		assertThat(publisher.events.get(3), is(instanceOf(LoadingLessResource.class)));
		assertThat(publisher.events.get(4), is(instanceOf(LessResourceNotFound.class)));
		assertThat(publisher.events.get(5), is(instanceOf(FinishedLessProcessing.class)));
	}

	private String getLessScript() throws Exception {
		return new String(Files.readAllBytes(Paths.get(getClass().getResource("/jj/css/assets/" + StylesheetResource.LESS_SCRIPT).toURI())), UTF_8);
	}
}
