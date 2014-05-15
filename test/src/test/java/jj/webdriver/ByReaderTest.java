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
package jj.webdriver;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.lang.annotation.Annotation;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class ByReaderTest {
	
	// this warning can be ignored! but unfortunately not suppressed?
	static class FakeBy implements By {
		
		private final String value;
		private final String id;
		private final String className;
		private final String selector;
		private final String xpath;
		
		FakeBy(String value, String id, String className, String selector, String xpath) {
			this.value = value == null ? "" : value;
			this.id = id == null ? "" : id;
			this.className = className == null ? "" : className;
			this.selector = selector == null ? "" : selector;
			this.xpath = xpath == null ? "" : xpath;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return By.class;
		}

		@Override
		public String value() {
			return value;
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public String className() {
			return className;
		}
		
		@Override
		public String cssSelector() {
			return selector;
		}
		
		@Override
		public String xpath() {
			return xpath;
		}
	}

	@Test
	public void testHappy() {
		ByReader br = new ByReader(new FakeBy("jay", "", "", "", ""));
		assertThat(br.needsResolution(), is(true));
		assertThat(br.type(), is("id"));
		assertThat(br.validateValueAsFormatterFor(), is(true));
		assertThat(br.value(), is("jay"));
		
		br = new ByReader(new FakeBy("jay-%d-%s-", null, null, null, null));
		assertThat(br.needsResolution(), is(true));
		assertThat(br.type(), is("id"));
		assertThat(br.validateValueAsFormatterFor(), is(false));
		assertThat(br.validateValueAsFormatterFor(" ", 0), is(false));
		assertThat(br.validateValueAsFormatterFor(0, " "), is(true));
		// extra args are ignored - this is the implementation of the
		// formatter, and it makes sense in a certain way
		assertThat(br.validateValueAsFormatterFor(0, " ", 0), is(true));
		
		assertThat(br.value(), is("jay-%d-%s-"));
		
		br = new ByReader(new FakeBy("", "jay", "", "", ""));
		assertThat(br.needsResolution(), is(false));
		assertThat(br.type(), is("id"));
		assertThat(br.validateValueAsFormatterFor(), is(true));
		assertThat(br.value(), is("jay"));
		
		br = new ByReader(new FakeBy("", "", "jay", "", ""));
		assertThat(br.needsResolution(), is(false));
		assertThat(br.type(), is("className"));
		assertThat(br.validateValueAsFormatterFor(), is(true));
		assertThat(br.value(), is("jay"));
		
		br = new ByReader(new FakeBy("", "", "", "jay", ""));
		assertThat(br.needsResolution(), is(false));
		assertThat(br.type(), is("cssSelector"));
		assertThat(br.validateValueAsFormatterFor(), is(true));
		assertThat(br.value(), is("jay"));
		
		br = new ByReader(new FakeBy("", "", "", "", "jay"));
		assertThat(br.needsResolution(), is(false));
		assertThat(br.type(), is("xpath"));
		assertThat(br.validateValueAsFormatterFor(), is(true));
		assertThat(br.value(), is("jay"));
	}

	
	@Test
	public void testSad() {
		
		boolean threw = true;
		try {
			new ByReader(new FakeBy(null, null, null, null, null));
			threw = false;
		} catch (AssertionError ae) {}
		
		assertTrue("didn't require at least one value", threw);
		
		threw = true;
		try {
			new ByReader(new FakeBy("id", "also id", null, null, null));
			threw = false;
		} catch (AssertionError ae) {}
		
		assertTrue("didn't require just one value", threw);
		
		threw = true;
		try {
			new ByReader(new FakeBy(null, "id", null, null, "xpath"));
			threw = false;
		} catch (AssertionError ae) {}
		
		assertTrue("didn't require just one value", threw);
	}
}
