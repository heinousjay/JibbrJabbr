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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.resolution.AppLocation.*;
import jj.http.server.HttpServerRequest;
import jj.http.server.HttpServerResponse;
import jj.http.server.resource.StaticResource;
import jj.http.server.uri.URIMatch;
import jj.resource.ResourceFinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class StaticServableTest {
	
	static final String ZERO_TXT = "0.txt";
	static final String NOT_ZERO_TXT = "../not-servable/0.txt";
	
	@Mock StaticResource resource;
	
	@Mock ResourceFinder resourceFinder;
	
	@Mock HttpServerRequest request;
	@Mock HttpServerResponse response;
	
	StaticServable ss;
	
	@Before
	public void before() {
		
		ss = new StaticServable(resourceFinder);
	}

	@Test
	public void testBasicOperation() throws Exception {
		
		given(request.uriMatch()).willReturn(new URIMatch("/" + ZERO_TXT));
		given(resourceFinder.loadResource(StaticResource.class, Base.and(Assets), ZERO_TXT)).willReturn(resource);
		given(resource.safeToServe()).willReturn(true);
		
		RequestProcessor rp = ss.makeRequestProcessor(request, response);
		
		assertThat(rp, is(notNullValue()));
	}
	
	@Test
	public void testOutsideApplicationIsRejected() throws Exception {
		
		given(request.uriMatch()).willReturn(new URIMatch("/" + NOT_ZERO_TXT));
		given(resourceFinder.loadResource(StaticResource.class, Base.and(Assets), NOT_ZERO_TXT)).willReturn(resource);
		
		RequestProcessor rp = ss.makeRequestProcessor(request, response);
		
		assertThat(rp, is(nullValue()));
	}
}
