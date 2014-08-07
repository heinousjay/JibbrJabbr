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
package jj.i18n;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class StringToLocaleConverterTest {
	
	StringToLocaleConverter slc = new StringToLocaleConverter();

	@Test
	public void test() {
		assertThat(slc.convert("en"), is(Locale.ENGLISH));
		assertThat(slc.convert("EN"), is(Locale.ENGLISH));
		assertThat(slc.convert("en-us"), is(Locale.US));
		assertThat(slc.convert("en_us"), is(Locale.US));
		assertThat(slc.convert("en-US"), is(Locale.US));
		assertThat(slc.convert("en_US"), is(Locale.US));
		assertThat(slc.convert("ja"), is(Locale.JAPANESE));
		assertThat(slc.convert("ja-JP"), is(Locale.JAPAN));
	}

}
