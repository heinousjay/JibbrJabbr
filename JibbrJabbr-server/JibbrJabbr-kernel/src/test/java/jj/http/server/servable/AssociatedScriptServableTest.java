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
import jj.resource.ScriptResource;
import jj.script.AssociatedScriptBundle;
import jj.script.ScriptBundleFinder;
import jj.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class AssociatedScriptServableTest extends ServableTestBase {

	AssociatedScriptServable as;
	@Mock ScriptBundleFinder finder;
	@Mock AssociatedScriptBundle bundle;
	@Mock ScriptResource scriptResource1;
	@Mock ScriptResource scriptResource2;
	@Mock ScriptResource scriptResource3;
	URIMatch match;
	
	@Before
	public void before() {
		as = new AssociatedScriptServable(configuration, finder);
		given(bundle.clientScriptResource()).willReturn(scriptResource1);
		given(bundle.sharedScriptResource()).willReturn(scriptResource2);
		given(bundle.serverScriptResource()).willReturn(scriptResource3);
	}
	
	private void configureMatch(String uri) {
		match = new URIMatch(uri);
		given(request.uriMatch()).willReturn(match);
		given(finder.forURIMatch(match)).willReturn(bundle);
	}
	
	@Test
	public void testIsMatchingRequest() {
		configureMatch("/1234567890123456789012345678901234567890/index.js");
		assertThat(as.isMatchingRequest(request), is(true));
		configureMatch("/1234567890123456789012345678901234567890/index.shared.js");
		assertThat(as.isMatchingRequest(request), is(true));
		configureMatch("/1234567890123456789012345678901234567890/index.server.js");
		assertThat(as.isMatchingRequest(request), is(false));
	}

	@Test
	public void testMakeRequestProcessor1() throws Exception {
		configureMatch("/1234567890123456789012345678901234567890/index.js");
		
		as.makeRequestProcessor(request, response).process();
		
		verify(response).sendCachedResource(scriptResource1);
	}

	@Test
	public void testMakeRequestProcessor2() throws Exception {
		configureMatch("/1234567890123456789012345678901234567890/index.shared.js");
		
		as.makeRequestProcessor(request, response).process();
		
		verify(response).sendCachedResource(scriptResource2);
	}

	@Test
	public void testMakeRequestProcessor3() throws Exception {
		configureMatch("/1234567890123456789012345678901234567890/index.server.js");
		
		assertThat(as.makeRequestProcessor(request, response), is(nullValue()));
	}
}
