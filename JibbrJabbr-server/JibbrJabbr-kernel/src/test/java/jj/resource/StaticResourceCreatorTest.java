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

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class StaticResourceCreatorTest extends ResourceBase {

	@Test
	public void test() throws Exception {
		
		doTest("blank.gif");
		doTest("style.css");
		doTest("helpers/jquery.fancybox-media.js");
		
		// this only works in test, since the class files in this directory
		doTest("jj/resource/StaticResourceCreatorTest.class"); 
	}
	
	private void doTest(final String baseName) throws Exception {
		StaticResource resource1 = testFileResource(baseName, new StaticResourceCreator(configuration));
		assertThat(resource1, is(notNullValue()));
		assertThat(resource1.mime(), is(MimeTypes.get(baseName)));
		assertThat(resource1.size(), is(Files.size(resource1.path())));
	}
	
	@Test
	public void testFileNotFound() throws Exception {
		// given
		String baseName = "not/a/real/baseName";
		StaticResourceCreator toTest = new StaticResourceCreator(configuration);
		
		// when
		try {
			toTest.create(baseName);
			
		// then
			fail("should have thrown");
		} catch (NoSuchFileException nsfe) {
			
		}
	}

}
