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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import jj.resource.html.HtmlResource;
import jj.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;

/**
 * @author jason
 *
 */
public class DocumentServableTest extends ServableTestBase {
	
	@Mock HtmlResource resource;
	@Mock Injector parentInjector;
	@Mock RequestProcessor requestProcessor;
	
	DocumentServable ds;
	
	@Before
	public void before() {
		given(parentInjector.createChildInjector(any(AbstractModule.class))).willReturn(parentInjector);
		given(parentInjector.getInstance(RequestProcessor.class)).willReturn(requestProcessor);
		
		ds = new DocumentServable(configuration, resourceFinder, parentInjector);
	}
	
	@Test
	public void testIsMatchingRequest() throws Exception {
		
		assertThat(ds.isMatchingRequest(new URIMatch("/")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/index")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/index.html")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/index/")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/../")), is(true));
		
		assertThat(ds.isMatchingRequest(new URIMatch("/index.css")), is(false));
		assertThat(ds.isMatchingRequest(new URIMatch("/index.whatever")), is(false));
		assertThat(ds.isMatchingRequest(new URIMatch("/index.html/bller.iller")), is(false));
	}
	

	@Test
	public void testBasicOperation() throws Exception {
		
		given(request.uri()).willReturn("/");
		given(resourceFinder.loadResource(HtmlResource.class, "index")).willReturn(resource);
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is(requestProcessor));
	}
	
	@Test
	public void testNormalizeURI() throws Exception {
		
		given(request.uri()).willReturn("/../servable/");
		given(resourceFinder.loadResource(HtmlResource.class, "index")).willReturn(resource);
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is(requestProcessor));
	}

	@Test
	public void testOutsideApplicationIsRejected() throws Exception {
		
		given(request.uri()).willReturn("/../not-servable/index.html");
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is(nullValue()));
	}
	
	@Test
	public void testNotFound() throws Exception {
		
		given(request.uri()).willReturn("/something");
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is(nullValue()));
	}
}
