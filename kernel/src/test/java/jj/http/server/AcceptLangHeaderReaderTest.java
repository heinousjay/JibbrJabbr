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
package jj.http.server;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Locale;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class AcceptLangHeaderReaderTest {

	HttpHeaders requestHeaders;
	
	AcceptLangHeaderReader arh;
	
	private void establishObjects(String value) {
		requestHeaders = new DefaultHttpHeaders();
		requestHeaders.set(HttpHeaders.Names.ACCEPT_LANGUAGE, value);
		arh = new AcceptLangHeaderReader(requestHeaders);
	}
	
	@Test
	public void test1() {
		establishObjects("da, en-gb;q=0.8, en;q=0.7");
		assertThat(arh.locales().size(), is(3));
		assertThat(arh.locales().get(0), is(new Locale("da")));
		assertThat(arh.locales().get(1), is(new Locale("en", "GB")));
		assertThat(arh.locales().get(2), is(new Locale("en")));
	}
	
	@Test
	public void test2() {
		establishObjects("en-US,en;q=0.8,ar;q=0.6");
		assertThat(arh.locales().size(), is(3));
		assertThat(arh.locales().get(0), is(new Locale("en", "US")));
		assertThat(arh.locales().get(1), is(new Locale("en")));
		assertThat(arh.locales().get(2), is(new Locale("ar")));
	}
	
	@Test
	public void test3() {
		establishObjects("en-US;q=0.4,en;q=0.7,ar;q=0.9");
		assertThat(arh.locales().size(), is(3));
		assertThat(arh.locales().get(0), is(new Locale("ar")));
		assertThat(arh.locales().get(1), is(new Locale("en")));
		assertThat(arh.locales().get(2), is(new Locale("en", "US")));
	}
	
	@Test
	public void testBadRequests() {
		establishObjects("this is not a value accept-lang string");
		assertThat(arh.isBadRequest(), is(true));
		
		establishObjects("da-da-da-da;q=2");
		assertThat(arh.isBadRequest(), is(true));
		
		establishObjects("da;q=2");
		assertThat(arh.isBadRequest(), is(true));
	}

}
