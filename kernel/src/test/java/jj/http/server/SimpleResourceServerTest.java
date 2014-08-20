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

import static jj.configuration.resolution.AppLocation.Base;
import static org.mockito.BDDMockito.*;

import io.netty.handler.codec.http.HttpHeaders;
import jj.execution.JJTask;
import jj.execution.Promise;
import jj.execution.TaskHelper;
import jj.http.uri.URIMatch;
import jj.resource.ResourceFinder;
import jj.resource.ResourceLoader;
import jj.resource.ServableResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleResourceServerTest {
	
	static final String SHA1 = "1234567890123456789012345678901234567890";
	static final String ZERO_TXT = "0.txt";
	static final String UNVERSIONED_URI = "/" + ZERO_TXT;
	static final String VERSIONED_URI = "/" + SHA1 + "/" + ZERO_TXT;
	static final String MIME = "mime";
	static final long LENGTH = 100L;
	
	@Mock ResourceFinder resourceFinder;
	@Mock ResourceLoader resourceLoader;
	
	@InjectMocks SimpleResourceServer srs;

	@Mock ServableResource resource;
	@Mock HttpServerRequest request;
	@Mock HttpServerResponse response;
	
	@Mock Promise promise;
	
	@Captor ArgumentCaptor<JJTask> taskCaptor;
	
	@Before
	public void before() {
		given(request.uriMatch()).willReturn(new URIMatch("/hi/there"));
		
		given(resourceLoader.loadResource(any(), any(), anyString())).willReturn(promise);
		
		given(resource.sha1()).willReturn(SHA1);
	}
	
	@Test
	public void testNotFound() throws Exception {
		
		srs.serve(ServableResource.class, request, response);
		
		verify(promise).then(taskCaptor.capture());
		
		TaskHelper.invoke(taskCaptor.getValue());
		
		verify(response).sendNotFound();
	}
	
	private void givenResourceRequest(String uri) {
		URIMatch match = new URIMatch(uri);
		
		given(request.uriMatch()).willReturn(match);
		given(resourceFinder.findResource(ServableResource.class, Base, match.path)).willReturn(resource);
	}
	
	@Test
	public void testLoadedResource() throws Exception {
		
		srs.serve(ServableResource.class, request, response);
		verify(promise).then(taskCaptor.capture());
		
		givenResourceRequest(UNVERSIONED_URI);
		
		TaskHelper.invoke(taskCaptor.getValue());
		
		verify(response).sendUncachableResource(resource);
	}
	
	@Test
	public void testStandardResponseUncachedResource() throws Exception {
		
		givenResourceRequest(UNVERSIONED_URI);
		
		srs.serve(ServableResource.class, request, response);
		
		verify(response).sendUncachableResource(resource);
	}
	
	@Test
	public void testStandardResponseCachedResource() throws Exception {
		
		givenResourceRequest(VERSIONED_URI);
		
		srs.serve(ServableResource.class, request, response);
		
		verify(response).sendCachableResource(resource);
	}
	
	@Test
	public void testStandardResponseNotModifiedCachable() throws Exception {
		
		givenResourceRequest(VERSIONED_URI);
		
		given(request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(true);
		given(request.header(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(SHA1);
		
		srs.serve(ServableResource.class, request, response);
		
		verify(response).sendNotModified(resource, true);
	}
	
	@Test
	public void testStandardResponseNotModifiedNotCachable() throws Exception {
		
		givenResourceRequest(UNVERSIONED_URI);
		
		given(request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(true);
		given(request.header(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(SHA1);
		
		srs.serve(ServableResource.class, request, response);
		
		verify(response).sendNotModified(resource, false);
	}
	
	@Test
	public void testStandardResponseTemporaryRedirect() throws Exception {
		
		givenResourceRequest(VERSIONED_URI);
		
		given(resource.sha1()).willReturn("some other sha1");

		srs.serve(ServableResource.class, request, response);
		
		verify(response).sendTemporaryRedirect(resource);
	}
	
	@Test
	public void testStandardResponseError() throws Exception {
		
		givenResourceRequest(VERSIONED_URI);
		
		given(resource.sha1()).willReturn("some other sha1");
		
		RuntimeException toThrow = new RuntimeException();
		
		given(response.sendTemporaryRedirect(resource)).willThrow(toThrow);

		srs.serve(ServableResource.class, request, response);
		
		verify(response).error(toThrow);
	}

}
