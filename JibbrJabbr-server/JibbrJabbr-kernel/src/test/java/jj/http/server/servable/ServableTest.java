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
package jj.http.server.servable;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import io.netty.handler.codec.http.HttpHeaders;

import java.io.IOException;

import jj.configuration.Configuration;
import jj.execution.IOThread;
import jj.http.HttpRequest;
import jj.http.HttpResponse;
import jj.resource.Resource;
import jj.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class ServableTest extends ServableTestBase {
	
	static final String SHA1 = "1234567890123456789012345678901234567890";
	static final String ZERO_TXT = "0.txt";
	static final String UNVERSIONED_URI = "/" + ZERO_TXT;
	static final String VERSIONED_URI = "/" + SHA1 + "/" + ZERO_TXT;
	static final String MIME = "mime";
	static final long LENGTH = 100L;
	
	static final class ServableImpl extends Servable<Resource> {

		/**
		 * @param configuration
		 */
		protected ServableImpl(Configuration configuration) {
			super(configuration);
		}

		@Override
		public boolean isMatchingRequest(URIMatch uriMatch) {
			return false;
		}

		@Override
		@IOThread
		public RequestProcessor makeRequestProcessor(HttpRequest request, HttpResponse response) throws IOException {
			return null;
		}

		@Override
		public Resource loadResource(URIMatch match) {
			return null;
		}

		@Override
		public Class<Resource> type() {
			return Resource.class;
		}
		
	}
	
	@Mock Resource resource;
	
	ServableImpl si;
	
	@Before
	public void before() {
		si = new ServableImpl(configuration);
	}

	@Test
	public void testIsServablePath() {
		
		assertThat(si.isServablePath(basePath.resolve("index.html")), is(true));
		assertThat(si.isServablePath(basePath.resolve("../not-servable/index.html")), is(false));
		
	}
	
	@Test
	public void testStandardResponseUncachedResource() throws Exception {
		
		given(request.uri()).willReturn(UNVERSIONED_URI);
		URIMatch match = new URIMatch(UNVERSIONED_URI);
		
		si.makeStandardRequestProcessor(request, response, match, resource).process();
		
		verify(response).sendUncachedResource(resource);
	}
	
	@Test
	public void testStandardResponseCachedResource() throws Exception {
		
		given(request.uri()).willReturn(VERSIONED_URI);
		URIMatch match = new URIMatch(VERSIONED_URI);
		
		given(resource.sha1()).willReturn(SHA1);
		
		si.makeStandardRequestProcessor(request, response, match, resource).process();
		
		verify(response).sendCachedResource(resource);
	}
	
	@Test
	public void testStandardResponseNotModifiedCachable() throws Exception {
		
		given(request.uri()).willReturn(VERSIONED_URI);
		URIMatch match = new URIMatch(VERSIONED_URI);
		
		given(resource.sha1()).willReturn(SHA1);
		
		given(request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(true);
		given(request.header(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(SHA1);
		
		si.makeStandardRequestProcessor(request, response, match, resource).process();
		
		verify(response).sendNotModified(resource, true);
	}
	
	@Test
	public void testStandardResponseNotModifiedNotCachable() throws Exception {
		
		given(request.uri()).willReturn(UNVERSIONED_URI);
		URIMatch match = new URIMatch(UNVERSIONED_URI);
		
		given(resource.sha1()).willReturn(SHA1);
		
		given(request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(true);
		given(request.header(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(SHA1);
		
		si.makeStandardRequestProcessor(request, response, match, resource).process();
		
		verify(response).sendNotModified(resource, false);
	}
	
	@Test
	public void testStandardResponseTemporaryRedirect() throws Exception {
		
		given(request.uri()).willReturn(VERSIONED_URI);
		URIMatch match = new URIMatch(VERSIONED_URI);
		
		given(resource.sha1()).willReturn("some other sha1");
		
		si.makeStandardRequestProcessor(request, response, match, resource).process();
		
		verify(response).sendTemporaryRedirect(resource);
	}
	
	@Test
	public void testStandardResponseError() throws Exception {
		
		given(request.uri()).willReturn(VERSIONED_URI);
		URIMatch match = new URIMatch(VERSIONED_URI);
		
		given(resource.sha1()).willReturn("some other sha1");
		
		RuntimeException toThrow = new RuntimeException();
		
		given(response.sendTemporaryRedirect(resource)).willThrow(toThrow);
		
		si.makeStandardRequestProcessor(request, response, match, resource).process();
		
		verify(response).error(toThrow);
	}

}
