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
		converters = new Converters(ConverterSetMaker.converters());
	}
	
	@Test
	public void testAssertions() {
		
		boolean threw = false;
		try {
			converters.convert("", getClass());
		} catch (AssertionError ae) {
			threw = true;
		}
		assertTrue("should have thrown", threw);
	}
	
	@Test
	public void testIdentity() {
		assertThat(converters.convert((Object)this, Object.class), is((Object)this));
		assertThat(converters.convert(this, ConvertersTest.class), is(this));
		assertThat(converters.convert("", String.class), is(""));
		assertThat(converters.convert(true, Boolean.TYPE), is(true));
		assertThat(converters.convert(false, Boolean.TYPE), is(false));
		assertThat(converters.convert(true, Boolean.class), is(true));
		assertThat(converters.convert(false, Boolean.class), is(false));
		assertThat(converters.convert('a', Character.class), is('a'));
		assertThat(converters.convert('a', Character.TYPE), is('a'));
		assertThat(converters.convert((byte)1, Byte.class), is((byte)1));
		assertThat(converters.convert((byte)1, Byte.TYPE), is((byte)1));
		assertThat(converters.convert((short)1, Short.class), is((short)1));
		assertThat(converters.convert((short)1, Short.TYPE), is((short)1));
		assertThat(converters.convert(1, Integer.class), is(1));
		assertThat(converters.convert(1, Integer.TYPE), is(1));
		assertThat(converters.convert(1L, Long.class), is(1L));
		assertThat(converters.convert(1L, Long.TYPE), is(1L));
		assertThat(converters.convert(1.0f, Float.class), is(1.0f));
		assertThat(converters.convert(1.0f, Float.TYPE), is(1.0f));
		assertThat(converters.convert(1.0, Double.class), is(1.0));
		assertThat(converters.convert(1.0, Double.TYPE), is(1.0));
	}
	
	@Test
	public void testConvertToString() {
		assertThat(converters.convert(1.0, String.class), is("1.0"));
		assertThat(converters.convert(true, String.class), is("true"));
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

	@Test
	public void testFromStringToInteger() {
		assertThat(converters.convert("1", int.class), is(1));
	}
	
	enum ConverterEnums {
		One,
		Two,
		Ready,
		Steady,
		What;
	}
	
	@Test
	public void testEnums() {
		assertThat(converters.convert("One", ConverterEnums.class), is(ConverterEnums.One));
		assertThat(converters.convert("Two", ConverterEnums.class), is(ConverterEnums.Two));
		assertThat(converters.convert("Ready", ConverterEnums.class), is(ConverterEnums.Ready));
		assertThat(converters.convert("Steady", ConverterEnums.class), is(ConverterEnums.Steady));
		assertThat(converters.convert("What", ConverterEnums.class), is(ConverterEnums.What));
	}
}
