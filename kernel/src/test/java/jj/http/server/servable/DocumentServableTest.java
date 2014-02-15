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
import jj.configuration.AppLocation;
import jj.http.server.servable.document.DocumentRequestProcessor;
import jj.resource.document.DocumentScriptEnvironment;
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
	
	@Mock DocumentScriptEnvironment resource;
	@Mock Injector parentInjector;
	@Mock DocumentRequestProcessor requestProcessor;
	
	DocumentServable ds;
	
	@Before
	public void before() {
		given(parentInjector.createChildInjector(any(AbstractModule.class))).willReturn(parentInjector);
		given(parentInjector.getInstance(DocumentRequestProcessor.class)).willReturn(requestProcessor);
		
		ds = new DocumentServable(app, resourceFinder, parentInjector);
	}
	
	@Test
	public void testIsMatchingRequest() throws Exception {
		
		assertThat(ds.isMatchingRequest(new URIMatch("/")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/index")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/index")), is(true));
		assertThat(ds.isMatchingRequest(new URIMatch("/index/")), is(true));
		
		assertThat(ds.isMatchingRequest(new URIMatch("/index.css")), is(false));
		assertThat(ds.isMatchingRequest(new URIMatch("/index.whatever")), is(false));
		assertThat(ds.isMatchingRequest(new URIMatch("/index.html/bller.iller")), is(false));
	}
	

	@Test
	public void testBasicOperation() throws Exception {
		
		given(request.uri()).willReturn("/");
		given(request.uriMatch()).willReturn(new URIMatch("/index"));
		given(resourceFinder.loadResource(DocumentScriptEnvironment.class, AppLocation.Virtual, "index")).willReturn(resource);
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is((RequestProcessor)requestProcessor));
	}

	@Test
	public void testOutsideApplicationIsRejected() throws Exception {
		
		given(request.uri()).willReturn("/../not-servable/index.html");
		given(request.uriMatch()).willReturn(new URIMatch(""));
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is(nullValue()));
	}
	
	@Test
	public void testNotFound() throws Exception {
		
		given(request.uri()).willReturn("/something");
		given(request.uriMatch()).willReturn(new URIMatch("/something"));
		
		RequestProcessor rp = ds.makeRequestProcessor(request, response);
		
		assertThat(rp, is(nullValue()));
	}
}
