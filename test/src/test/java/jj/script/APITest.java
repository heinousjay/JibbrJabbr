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

import static org.junit.Assert.*;

import jj.App;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.testing.JibbrJabbrTestServer;
import jj.testing.TestHttpClient;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
@Subscriber
public class APITest {

	@Rule
	public JibbrJabbrTestServer server = new JibbrJabbrTestServer(App.api)
		.injectInstance(this);
	
	
	
	@Listener
	void error(ScriptError scriptError) {
		error = true;
	}
	
	private boolean error;
	
	@Test
	public void test() throws Exception {
		
		TestHttpClient index = server.get("/");
		
		System.out.println(index.contentsString());
		
		assertFalse(error);
	}
	
}