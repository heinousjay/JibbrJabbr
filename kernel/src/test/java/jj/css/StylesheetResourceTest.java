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
import static jj.configuration.resolution.AppLocation.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.resource.DependentsExtractor.dependents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.resource.MockAbstractResourceDependencies;
import jj.resource.NoSuchResourceException;
import jj.resource.stat.ic.StaticResource;
import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;
import jj.script.module.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class StylesheetResourceTest {
	
	MockAbstractResourceDependencies dependencies;
	RealRhinoContextProvider contextProvider;
	ScriptableObject global;
	@Mock CssReferenceVersionProcessor processor;
	
	@Mock StaticResource cssResource;
	@Mock ScriptResource lessScriptResource;
	
	Path cssPath;
	
	@Before
	public void before() throws Exception {
		dependencies = new MockAbstractResourceDependencies(Base);
		contextProvider = new RealRhinoContextProvider();
		
		cssPath = Paths.get(StylesheetResourceTest.class.getResource("/jj/css/test/test.css").toURI());
		given(cssResource.path()).willReturn(cssPath);
	}
	
	private StylesheetResource newStylesheet(String name) {
		return new StylesheetResource(dependencies, name, contextProvider, global, processor);
	}

	@Test
	public void testNotFound() {
		String name = "test.css";
		
		try {
			newStylesheet(name);
			fail();
		} catch (NoSuchResourceException nsre) {
			// yay
		}
		
		verify(dependencies.resourceFinder()).loadResource(StaticResource.class, Base, name);
		verify(dependencies.resourceFinder()).loadResource(LessResource.class, Base, "test.less");
	}
	
	@Test
	public void testCssFound() {
		String name = "test.css";
		
		given(dependencies.resourceFinder().loadResource(StaticResource.class, Base, name)).willReturn(cssResource);
		given(processor.fixUris(anyString(), isA(StylesheetResource.class))).willReturn("");
		
		StylesheetResource sr = newStylesheet(name);
		
		verify(cssResource).addDependent(sr);
	}
	
	@Test
	public void testLessFound() throws Exception {
		
		// given
		String name = "test.css";
		
		LessResource less1 = new LessResource(dependencies, "test.less", cssPath.resolveSibling("test.less"));
		given(dependencies.resourceFinder().loadResource(LessResource.class, Base, "test.less")).willReturn(less1);
		LessResource less2 = new LessResource(dependencies, "test.less", cssPath.resolveSibling("test2.less"));
		given(dependencies.resourceFinder().loadResource(LessResource.class, Base, "test2.less")).willReturn(less2);
		given(dependencies.resourceFinder().loadResource(ScriptResource.class, Assets, StylesheetResource.LESS_SCRIPT)).willReturn(lessScriptResource);
		
		given(processor.fixUris(anyString(), isA(StylesheetResource.class))).willAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return (String)invocation.getArguments()[0];
			}
		});
		try (RhinoContext context = contextProvider.get().withoutContinuations()) {
			global = context.initStandardObjects();
			given(lessScriptResource.script()).willReturn(context.compileString(getLessScript(), "less script"));
			
			
		// when
			StylesheetResource sr = newStylesheet(name);
			
			
		// then
			verify(lessScriptResource).addDependent(sr);
			assertTrue(dependents(less1).contains(sr));
			assertTrue(dependents(less2).contains(sr));
			
			assertThat(sr.bytes().toString(UTF_8), is(new String(Files.readAllBytes(cssPath), UTF_8)));
		}
		
		verify(dependencies.publisher()).publish(isA(StartingLessProcessing.class));
		verify(dependencies.publisher()).publish(isA(FinishedLessProcessing.class));
		verify(dependencies.publisher(), times(3)).publish(isA(LoadingLessResource.class));
		verify(dependencies.publisher()).publish(isA(LessResourceNotFound.class));
	}

	private String getLessScript() throws Exception {
		return new String(Files.readAllBytes(Paths.get(getClass().getResource("/jj/css/" + StylesheetResource.LESS_SCRIPT).toURI())), UTF_8);
	}
}
