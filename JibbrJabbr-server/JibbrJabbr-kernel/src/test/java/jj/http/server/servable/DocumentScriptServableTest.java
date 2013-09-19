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
import io.netty.handler.codec.http.HttpHeaders;
import jj.resource.document.ScriptResource;
import jj.script.DocumentScriptExecutionEnvironment;
import jj.script.ScriptExecutionEnvironmentFinder;
import jj.uri.URIMatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author jason
 *
 */
public class DocumentScriptServableTest extends ServableTestBase {
	
	static final String SHA1 = "1234567890123456789012345678901234567890";

	DocumentScriptServable as;
	@Mock ScriptExecutionEnvironmentFinder finder;
	@Mock DocumentScriptExecutionEnvironment executionEnvironment;
	@Mock ScriptResource scriptResource1;
	@Mock ScriptResource scriptResource2;
	@Mock ScriptResource scriptResource3;
	URIMatch match;
	
	@Before
	public void before() {
		as = new DocumentScriptServable(configuration, finder);
		given(executionEnvironment.clientScriptResource()).willReturn(scriptResource1);
		given(executionEnvironment.sharedScriptResource()).willReturn(scriptResource2);
		given(executionEnvironment.serverScriptResource()).willReturn(scriptResource3);
		
		given(scriptResource1.sha1()).willReturn(SHA1);
		given(scriptResource2.sha1()).willReturn(SHA1);
		given(scriptResource3.sha1()).willReturn(SHA1);
		
	}
	
	private void configureMatch(String uri, boolean withValidationHeader) {
		match = new URIMatch(uri);
		given(request.uriMatch()).willReturn(match);
		given(finder.forURIMatch(match)).willReturn(executionEnvironment);
		
		if (withValidationHeader) {
			given(request.hasHeader(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(true);
			given(request.header(HttpHeaders.Names.IF_NONE_MATCH)).willReturn(match.sha1);
		}
	}
	
	@Test
	public void testIsMatchingRequest() {
		configureMatch("/" + SHA1 + "/index.js", false);
		assertThat(as.isMatchingRequest(match), is(true));
		configureMatch("/" + SHA1 + "/index.shared.js", false);
		assertThat(as.isMatchingRequest(match), is(true));
		configureMatch("/" + SHA1 + "/index.server.js", false);
		assertThat(as.isMatchingRequest(match), is(false));
	}

	@Test
	public void testMakeRequestProcessorClientCachedResponse() throws Exception {
		configureMatch("/" + SHA1 + "/index.js", false);
		
		as.makeRequestProcessor(request, response).process();
		
		verify(response).sendCachedResource(scriptResource1);
	}

	@Test
	public void testMakeRequestProcessorSharedCachedResponse() throws Exception {
		configureMatch("/" + SHA1 + "/index.shared.js", false);
		
		as.makeRequestProcessor(request, response).process();
		
		verify(response).sendCachedResource(scriptResource2);
	}

	@Test
	public void testMakeRequestProcessorClientNotModifiedResponse() throws Exception {
		configureMatch("/" + SHA1 + "/index.js", true);
		
		as.makeRequestProcessor(request, response).process();
		
		verify(response).sendNotModified(scriptResource1, true);
	}

	@Test
	public void testMakeRequestProcessorSharedNotModifiedResponse() throws Exception {
		configureMatch("/" + SHA1 + "/index.shared.js", true);
		
		as.makeRequestProcessor(request, response).process();
		
		verify(response).sendNotModified(scriptResource2, true);
	}

	@Test
	public void testMakeRequestProcessorServerNoResponse() throws Exception {
		configureMatch("/" + SHA1 + "/index.server.js", false);
		
		assertThat(as.makeRequestProcessor(request, response), is(nullValue()));
	}
}
