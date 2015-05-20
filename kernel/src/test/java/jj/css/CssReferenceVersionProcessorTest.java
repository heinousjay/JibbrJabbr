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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.application.AppLocation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.application.MockApplication;
import jj.http.server.resource.StaticResource;
import jj.http.server.resource.StaticResourceMaker;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.ResourceFinder;

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
public class CssReferenceVersionProcessorTest {
	
	private static final String REPLACEMENT_CSS = "replacement.css";
	private static final String TEST_CSS = "test.css";
	private static final String BOX_ICON = "images/box-icon.png";
	private static final String SOX_ICON = "images/sox-icon.png";
	private static final String ROX_ICON = "images/other/rox-icon.png";
	
	Path basePath;

	ResourceFinder resourceFinder;
	MockApplication app;
	
	@Mock StylesheetResource testCss;
	
	MockAbstractResourceDependencies dependencies;
	
	@Before
	public void before() throws Exception {
		basePath = Paths.get(getClass().getResource("/jj/css/test").toURI());
		app = new MockApplication(basePath);
		
		dependencies = new MockAbstractResourceDependencies(Base, REPLACEMENT_CSS);
		
		resourceFinder = dependencies.resourceFinder();
	}

	@Test
	public void testProcessUrls() throws Exception {
		
		CssReferenceVersionProcessor processor = new CssReferenceVersionProcessor(app, resourceFinder);
		
		// given
		StaticResource replacement = spy(StaticResourceMaker.make(app, Base, REPLACEMENT_CSS));
		given(resourceFinder.loadResource(StaticResource.class, Base, REPLACEMENT_CSS)).willReturn(replacement);
		
		StylesheetResource stylesheet = new StylesheetResource(dependencies, null, null, processor, null, app);
		
		
		given(testCss.serverPath()).willReturn("/11f2a2c59c6b8c8be4287d441ace20d0afa43e0e/test.css");
		given(testCss.path()).willReturn(basePath.resolve(TEST_CSS));
		given(resourceFinder.loadResource(StylesheetResource.class, Base, TEST_CSS)).willReturn(testCss);
		
		StaticResource box = spy(StaticResourceMaker.make(app, Base, BOX_ICON));
		StaticResource rox = spy(StaticResourceMaker.make(app, Base, ROX_ICON));
		StaticResource sox = spy(StaticResourceMaker.make(app, Base, SOX_ICON));
		given(resourceFinder.loadResource(StaticResource.class, Base, BOX_ICON)).willReturn(box);
		given(resourceFinder.loadResource(StaticResource.class, Base, ROX_ICON)).willReturn(rox);
		given(resourceFinder.loadResource(StaticResource.class, Base, SOX_ICON)).willReturn(sox);
		
		String inputString = new String(Files.readAllBytes(basePath.resolve(REPLACEMENT_CSS)), UTF_8);
		
		// when
		String outputString = processor.fixUris(inputString, stylesheet);
		
		String expected = new String(Files.readAllBytes(basePath.resolve("url_replacement_output.txt")), UTF_8);
		
		assertThat(outputString, is(expected));
		
		verify(testCss).addDependent(stylesheet);
		verify(box).addDependent(stylesheet);
		verify(rox).addDependent(stylesheet);
		verify(sox, times(2)).addDependent(stylesheet); // this one is included twice in the css
	}
}
