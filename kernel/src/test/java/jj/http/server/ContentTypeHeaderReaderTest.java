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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class ContentTypeHeaderReaderTest {

	HttpHeaders requestHeaders;
	
	ContentTypeHeaderReader cth;
	
	private void establishObjects(String value) {
		requestHeaders = new DefaultHttpHeaders();
		requestHeaders.set(HttpHeaderNames.CONTENT_TYPE, value);
		cth = new ContentTypeHeaderReader(requestHeaders);
	}

	@Test
	public void test1() {
		establishObjects("text/html; charset=UTF-8");
		
		assertThat(cth.isBadRequest(), is(false));
		assertThat(cth.isMultipart(), is(false));
		assertThat(cth.isText(), is(true));
		assertThat(cth.charset(), is(UTF_8));
		assertThat(cth.boundary(), is(nullValue()));
		assertThat(cth.mimeType(), is("text/html"));
		
		
		establishObjects("text/html");

		assertThat(cth.isBadRequest(), is(false));
		assertThat(cth.isMultipart(), is(false));
		assertThat(cth.isText(), is(false));
		assertThat(cth.charset(), is(nullValue()));
		assertThat(cth.boundary(), is(nullValue()));
		assertThat(cth.mimeType(), is("text/html"));
		
		
		establishObjects("application/json; charset=UTF-8");

		assertThat(cth.isBadRequest(), is(false));
		assertThat(cth.isMultipart(), is(false));
		assertThat(cth.isText(), is(true));
		assertThat(cth.charset(), is(UTF_8));
		assertThat(cth.boundary(), is(nullValue()));
		assertThat(cth.mimeType(), is("application/json"));
		
		
		establishObjects("multipart/form-data; boundary=-111kjnsdnajf9ajdf9jasdf");

		assertThat(cth.isBadRequest(), is(false));
		assertThat(cth.isMultipart(), is(true));
		assertThat(cth.isText(), is(false));
		assertThat(cth.charset(), is(nullValue()));
		assertThat(cth.boundary(), is("-111kjnsdnajf9ajdf9jasdf"));
		assertThat(cth.mimeType(), is("multipart/form-data"));
	}

	@Test
	public void testBadRequest() {
		establishObjects("gibberish.  nothing makes sense here at all");
		
		assertThat(cth.isBadRequest(), is(true));
		assertThat(cth.isMultipart(), is(false));
		assertThat(cth.isText(), is(false));
		assertThat(cth.charset(), is(nullValue()));
		assertThat(cth.boundary(), is(nullValue()));
		assertThat(cth.mimeType(), is(nullValue()));
	}
}
