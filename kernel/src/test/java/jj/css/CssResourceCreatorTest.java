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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.resource.DependentsExtractor.dependents;

import java.nio.file.Files;
import java.nio.file.Path;

import jj.configuration.resolution.AppLocation;
import jj.css.CssResource;
import jj.css.CssResourceCreator;
import jj.css.LessProcessor;
import jj.event.Publisher;
import jj.resource.AbstractResource.Dependencies;
import jj.resource.Resource;
import jj.resource.ResourceBase;
import jj.resource.ResourceKey;
import jj.resource.ResourceFinder;
import jj.resource.ResourceMaker;
import jj.resource.stat.ic.StaticResource;
import jj.script.RealRhinoContextProvider;
import jj.util.SHA1Helper;

import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class CssResourceCreatorTest extends ResourceBase<CssResource, CssResourceCreator> {
	
	private static final String BOX_ICON = "../jj/images/box-icon.png";
	private static final String ROX_ICON = "../jj/resource/images/rox-icon.png";
	private static final String SOX_ICON = "../jj/resource/sox-icon.png";
	
	@Override
	protected String name() {
		return "../jj/resource/test.css";
	}
	
	protected ResourceKey cacheKey(String baseName) {
		return new ResourceKey(CssResource.class, path(baseName).toUri());
	}
	
	protected Path path() {
		return path(name());
	}
	
	protected Path path(String baseName) {
		return appPath.resolve(baseName).normalize();
	}
	
	@Override
	protected CssResource resource() throws Exception {
		return resource(name());
	}
	
	protected CssResource resource(String name) throws Exception {
		return new CssResource(new Dependencies(cacheKey(name), AppLocation.Base), name, path(name), false);
	}
	
	@Override
	protected CssResourceCreator toTest() {
		return new CssResourceCreator(app, lessProcessor, resourceFinder, creator);
	}
	
	ResourceMaker resourceMaker;
	LessProcessor lessProcessor;
	@Mock ResourceFinder resourceFinder;
	
	protected void before() throws Exception {
		lessProcessor = spy(new LessProcessor(app, new RealRhinoContextProvider(), mock(Publisher.class)));
		resourceMaker = new ResourceMaker(configuration, app, logger);
	}

	@Test
	public void testCreation() throws Exception {
		
		CssResource css = toTest.create(location(), name());
		assertThat(css.sha1(), is(SHA1Helper.keyFor(Files.readAllBytes(css.path()))));
		
		CssResource less = toTest.create(location(), name(), true);
		
		// just to prove that one of these was actually less processed
		verify(lessProcessor).process(name().replace("css", "less"));
		
		// and we should end up with the same thing
		assertThat(css.bytes().compareTo(less.bytes()), is(0));
	}

	@Test
	public void testProcessUrls() throws Exception {
		
		// given
		CssResource testResource = toTest.create(location(), name());
		
		given(resourceFinder.loadResource(CssResource.class, AppLocation.Base, name())).willReturn(testResource);
		
		StaticResource box = resourceMaker.makeStatic(AppLocation.Base, BOX_ICON);
		StaticResource rox = resourceMaker.makeStatic(AppLocation.Base, ROX_ICON);
		StaticResource sox = resourceMaker.makeStatic(AppLocation.Base, SOX_ICON);
		given(resourceFinder.loadResource(StaticResource.class, AppLocation.Base, BOX_ICON)).willReturn(box);
		given(resourceFinder.loadResource(StaticResource.class, AppLocation.Base, ROX_ICON)).willReturn(rox);
		given(resourceFinder.loadResource(StaticResource.class, AppLocation.Base, SOX_ICON)).willReturn(sox);
		
		String replacement = "../jj/resource/replacement.css";
		
		configureInjector(resource(replacement));
		
		// when
		CssResource css = toTest.create(location(), replacement);
		
		// then
		assertThat(css, is(notNullValue()));
		
		assertThat(dependents(testResource), contains((Resource)css));
		assertThat(dependents(box), contains((Resource)css));
		assertThat(dependents(rox), contains((Resource)css));
		assertThat(dependents(sox), contains((Resource)css));
		
		byte[] bytes = new byte[css.bytes().readableBytes()];
		css.bytes().readBytes(bytes);
		
		// if this starts failing, maybe a dependency changed, so check the output
		//System.out.println(css.bytes().toString(java.nio.charset.StandardCharsets.UTF_8));
		
		assertThat(bytes, is(Files.readAllBytes(testResource.path().resolveSibling("url_replacement_output.txt"))));
		
		// this will happen another way? maybe the emergency logger will become the server logger, which guaranteed to always be at
		// at least error? info?  maybe...
		//verify(logger).warn(anyString(), eq(css.name()), eq("../jj/resource/not-found-thing.jpg"), eq("url(not-found-thing.jpg)"));
	}
}
