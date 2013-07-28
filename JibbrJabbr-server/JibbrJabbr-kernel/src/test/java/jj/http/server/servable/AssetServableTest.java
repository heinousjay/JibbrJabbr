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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class AssetServableTest extends ServableTestBase {
	
	String uri1 = "uri1.asset";
	String uri2 = "uri2.asset";

	@Mock AssetResource resource;
	
	AssetServable as;
	
	@Before
	public void before() {
		as = new AssetServable(configuration, resourceFinder);
		given(resourceFinder.findResource(AssetResource.class, uri1)).willReturn(resource);
	}
	
	@Test
	public void testIsMatchingRequest() {
		
		given(request.uri()).willReturn("/" + uri1);
		assertThat(as.isMatchingRequest(request), is(true));
		
		given(request.uri()).willReturn("/" + uri2);
		assertThat(as.isMatchingRequest(request), is(false));
	}

	@Test
	public void testMakeRequestProcessor() throws Exception {

		given(request.uri()).willReturn("/" + uri1);
		assertThat(as.makeRequestProcessor(request, response), is(notNullValue()));

		given(request.uri()).willReturn("/" + uri2);
		assertThat(as.makeRequestProcessor(request, response), is(nullValue()));
		
	}
}
