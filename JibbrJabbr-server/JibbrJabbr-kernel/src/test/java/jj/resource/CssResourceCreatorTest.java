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

import java.nio.file.Files;
import java.nio.file.Path;

import jj.SHA1Helper;
import jj.execution.ExecutionTrace;
import jj.script.RealRhinoContextMaker;

import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
public class CssResourceCreatorTest extends ResourceBase<CssResource, CssResourceCreator> {
	
	private static final String BOX_ICON = "jj/images/box-icon.png";
	private static final String ROX_ICON = "jj/resource/images/rox-icon.png";
	private static final String SOX_ICON = "jj/resource/sox-icon.png";
	
	@Override
	protected String baseName() {
		return "jj/resource/test.css";
	}
	
	protected ResourceCacheKey cacheKey(String baseName) {
		return new ResourceCacheKey(CssResource.class, path(baseName).toUri());
	}
	
	@Override
	protected Path path() {
		return path(baseName());
	}
	
	protected Path path(String baseName) {
		return appPath.resolve(baseName);
	}
	
	@Override
	protected CssResource resource() throws Exception {
		return resource(baseName());
	}
	
	protected CssResource resource(String baseName) throws Exception {
		return new CssResource(cacheKey(baseName), baseName, path(baseName), false);
	}
	
	@Override
	protected CssResourceCreator toTest() {
		return new CssResourceCreator(configuration, lessProcessor, resourceFinder, logger, instanceModuleCreator);
	}
	
	LessProcessor lessProcessor;
	@Mock ResourceFinder resourceFinder;
	@Mock Logger logger;
	
	protected void before() throws Exception {
		lessProcessor = spy(new LessProcessor(configuration, mock(ExecutionTrace.class), new RealRhinoContextMaker()));
	}

	@Test
	public void testCreation() throws Exception {
		
		CssResource css = toTest.create(baseName());
		assertThat(css.sha1(), is(SHA1Helper.keyFor(Files.readAllBytes(css.path()))));
		
		CssResource less = toTest.create(baseName(), true);
		
		// just to prove that one of these was actually less processed
		verify(lessProcessor).process(baseName().replace("css", "less"));
		
		// and we should end up with the same thing
		assertThat(css.bytes().compareTo(less.bytes()), is(0));
	}
	
	private StaticResource make(StaticResourceCreator src, String name) throws Exception {
		return new StaticResource(src.cacheKey(name), src.path(name), name);
	}

	@Test
	public void testProcessUrls() throws Exception {
		
		// given
		CssResource testResource = toTest.create(baseName());
		
		given(resourceFinder.loadResource(CssResource.class, baseName())).willReturn(testResource);
		
		StaticResourceCreator src = new StaticResourceCreator(configuration, instanceModuleCreator);
		StaticResource box = make(src, BOX_ICON);
		StaticResource rox = make(src, ROX_ICON);
		StaticResource sox = make(src, SOX_ICON);
		given(resourceFinder.loadResource(StaticResource.class, BOX_ICON)).willReturn(box);
		given(resourceFinder.loadResource(StaticResource.class, ROX_ICON)).willReturn(rox);
		given(resourceFinder.loadResource(StaticResource.class, SOX_ICON)).willReturn(sox);
		
		String replacement = "jj/resource/replacement.css";
		
		configureInjector(resource(replacement));
		
		// when
		CssResource css = toTest.create(replacement);
		
		// then
		assertThat(css, is(notNullValue()));
		
		assertThat(testResource.dependents(), contains((AbstractResource)css));
		assertThat(box.dependents(), contains((AbstractResource)css));
		assertThat(rox.dependents(), contains((AbstractResource)css));
		assertThat(sox.dependents(), contains((AbstractResource)css));
		
		byte[] bytes = new byte[css.bytes().readableBytes()];
		css.bytes().readBytes(bytes);
		
		// if this starts failing, maybe a dependency changed, so check the output
		// System.out.println(css.bytes().toString(UTF_8));
		
		assertThat(bytes, is(Files.readAllBytes(testResource.path().resolveSibling("url_replacement_output.txt"))));
		
		verify(logger).warn(anyString(), eq(css.baseName()), eq("jj/resource/not-found-thing.jpg"), eq("url(not-found-thing.jpg)"));
	}
}
