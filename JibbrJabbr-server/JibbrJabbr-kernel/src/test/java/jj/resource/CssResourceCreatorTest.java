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
import jj.SHA1Helper;
import jj.execution.ExecutionTrace;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
public class CssResourceCreatorTest extends ResourceBase {
	
	private static final String BOX_ICON = "jj/images/box-icon.png";
	private static final String ROX_ICON = "jj/resource/images/rox-icon.png";
	private static final String SOX_ICON = "jj/resource/sox-icon.png";
	
	LessProcessor lessProcessor;
	@Mock ResourceFinder resourceFinder;
	@Mock Logger logger;
	
	CssResourceCreator crc;
	
	@Before
	public void before() throws Exception {
		lessProcessor = spy(new LessProcessor(configuration, mock(ExecutionTrace.class)));
		crc = new CssResourceCreator(configuration, lessProcessor, resourceFinder, logger);
	}

	@Test
	public void testCreation() throws Exception {
		
		CssResource css = testFileResource("jj/resource/test.css", crc);
		assertThat(css.sha1(), is(SHA1Helper.keyFor(Files.readAllBytes(css.path()))));
		
		CssResource less = crc.create("jj/resource/test.css", Boolean.TRUE);
		
		// just to prove that one of these was actually less processed
		verify(lessProcessor).process("jj/resource/test.less");
		
		// and we should end up with the same thing
		assertThat(css.bytes().compareTo(less.bytes()), is(0));
	}

	@Test
	public void testProcessUrls() throws Exception {
		
		// given
		CssResource test = crc.create("jj/resource/test.css");
		given(resourceFinder.loadResource(CssResource.class, "jj/resource/test.css")).willReturn(test);
		StaticResourceCreator src = new StaticResourceCreator(configuration);
		StaticResource box = src.create(BOX_ICON);
		StaticResource rox = src.create(ROX_ICON);
		StaticResource sox = src.create(SOX_ICON);
		given(resourceFinder.loadResource(StaticResource.class, BOX_ICON)).willReturn(box);
		given(resourceFinder.loadResource(StaticResource.class, ROX_ICON)).willReturn(rox);
		given(resourceFinder.loadResource(StaticResource.class, SOX_ICON)).willReturn(sox);
		
		// when
		CssResource css = crc.create("jj/resource/empty.css");
		
		// then
		assertThat(css, is(notNullValue()));
		
		assertThat(test.dependents(), contains((AbstractResource)css));
		assertThat(box.dependents(), contains((AbstractResource)css));
		assertThat(rox.dependents(), contains((AbstractResource)css));
		assertThat(sox.dependents(), contains((AbstractResource)css));
		
		byte[] bytes = new byte[css.bytes().readableBytes()];
		css.bytes().readBytes(bytes);
		
		// if this starts failing, maybe a dependency changed, so check the output
		// System.out.println(css.bytes.toString(UTF_8));
		
		assertThat(bytes, is(Files.readAllBytes(test.path().resolveSibling("url_replacement_output.txt"))));
		
		verify(logger).warn(anyString(), eq(css.baseName()), eq("jj/resource/not-found-thing.jpg"), eq("url(not-found-thing.jpg)"));
	}
}
