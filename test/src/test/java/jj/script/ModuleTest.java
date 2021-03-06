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
package jj.script;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import javax.inject.Inject;

import jj.App;
import jj.ServerRoot;
import jj.http.server.EmbeddedHttpRequest;
import jj.http.server.EmbeddedHttpResponse;
import jj.http.server.EmbeddedHttpServer;
import jj.testing.JibbrJabbrTestServer;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class ModuleTest {
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(ServerRoot.one, App.module).injectInstance(this);
	
	@Inject EmbeddedHttpServer server;
	
	@Test
	public void moduleHierarchies() throws Throwable {
		
		EmbeddedHttpResponse client = server.request(new EmbeddedHttpRequest("/deep/nested")).await(2, SECONDS);
		
		Document document = Parser.parse(client.bodyContentAsString(), "");
		
		assertThat(document.select("title").text(), is("titled"));
	}
}
