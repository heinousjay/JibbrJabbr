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
import static org.mockito.Mockito.*;

import java.nio.file.Files;

import jj.ExecutionTrace;
import jj.SHA1Helper;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class CssResourceCreatorTest extends ResourceBase {
	
	LessProcessor lessProcessor;
	
	@Before
	public void before() throws Exception {
		lessProcessor = spy(new LessProcessor(configuration, mock(ExecutionTrace.class)));
	}

	@Test
	public void test() throws Exception {
		CssResourceCreator toTest = new CssResourceCreator(configuration, lessProcessor);
		
		CssResource css = testFileResource("jj/resource/test.css", toTest);
		assertThat(css.sha1(), is(SHA1Helper.keyFor(Files.readAllBytes(css.path()))));
		
		CssResource less = toTest.create("jj/resource/test.css", Boolean.TRUE);
		
		// just to prove that one of these was actually less processed
		verify(lessProcessor).process("jj/resource/test.less");
		
		// and we should end up with the same thing
		assertThat(css.bytes().compareTo(less.bytes()), is(0));
		
	}

}
