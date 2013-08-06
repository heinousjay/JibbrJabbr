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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.resource.AssetResource;
import jj.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class AssetServableTest extends ServableTestBase {
	
	URIMatch uri1 = new URIMatch("/jj.js");
	URIMatch uri2 = new URIMatch("/uri2.asset");

	@Mock AssetResource resource;
	
	AssetServable as;
	
	@Before
	public void before() {
		as = new AssetServable(configuration, resourceFinder);
		given(resourceFinder.loadResource(AssetResource.class, uri1.baseName)).willReturn(resource);
	}
	
	@Test
	public void testIsMatchingRequest() {
		
		assertThat(as.isMatchingRequest(uri1), is(true));
		
		assertThat(as.isMatchingRequest(uri2), is(false));
	}

	@Test
	public void testMakeRequestProcessor() throws Exception {

		given(request.uriMatch()).willReturn(uri1);
		assertThat(as.makeRequestProcessor(request, response), is(notNullValue()));

		given(request.uriMatch()).willReturn(uri2);
		assertThat(as.makeRequestProcessor(request, response), is(nullValue()));
		
	}
}
