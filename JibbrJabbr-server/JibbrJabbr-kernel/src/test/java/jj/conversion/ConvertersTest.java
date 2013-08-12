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
package jj.conversion;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import jj.JJ;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ConvertersTest {
	
	Converters converters;
	
	@Before
	public void before() {
		converters = new Converters();
	}
	
	@Test
	public void testAssertions() {
		
		boolean threw = false;
		try {
			converters.convert(this, Object.class);
		} catch (AssertionError ae) {
			threw = true;
		}
		assertTrue("should have thrown", threw);
		
		threw = false;
		try {
			converters.convert("", getClass());
		} catch (AssertionError ae) {
			threw = true;
		}
		assertTrue("should have thrown", threw);
	}

	@Test
	public void testFromStringToPath() {
		Path testPath = Paths.get(JJ.uri(ConvertersTest.class));
		
		Path path = converters.convert(testPath.toString(), Path.class);
		
		assertThat(path, is(testPath));
	}
	
	@Test
	public void testFromStringToBoolean() {
		assertTrue(converters.convert("true", Boolean.class));
		assertFalse(converters.convert("false", Boolean.class));
		assertFalse(converters.convert("", Boolean.class));
		assertTrue(converters.convert("true", Boolean.TYPE));
		assertFalse(converters.convert("false", Boolean.TYPE));
		assertFalse(converters.convert("", Boolean.TYPE));
	}

}