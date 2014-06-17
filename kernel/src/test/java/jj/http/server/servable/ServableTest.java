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

import jj.configuration.resolution.AppLocation;
import jj.configuration.resolution.Application;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.server.uri.URIMatch;
import jj.resource.FileResource;
import jj.resource.ResourceThread;
import jj.resource.Resource;

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
		protected ServableImpl(Application app) {
			super(app);
		}

		@Override
		public boolean isMatchingRequest(URIMatch uriMatch) {
			return false;
		}

		@Override
		@ResourceThread
		public RequestProcessor makeRequestProcessor(HttpServerRequest request, HttpServerResponse response) throws IOException {
			return null;
		}

		@Override
		public Resource loadResource(URIMatch match) {
			return null;
		}
		
	}
	
	@Mock FileResource resource;
	
	ServableImpl si;
	
	@Before
	public void before() {
		si = new ServableImpl(app);
	}

	@Test
	public void testIsServablePath() {
		
		given(resource.path()).willReturn(appPath.resolve("index.html"));
		given(resource.base()).willReturn(AppLocation.Public);
		
		assertThat(si.isServableResource(resource), is(true));
		
		given(resource.path()).willReturn(appPath.resolve("../not-servable/index.html"));
		
		assertThat(si.isServableResource(resource), is(false));
		
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
