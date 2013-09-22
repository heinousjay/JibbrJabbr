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
package jj.testing;

import static java.util.concurrent.TimeUnit.HOURS;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class RunAPage {

	@Rule
	public JJAppTest app = new JJAppTest(App.path);
	
	@Test
	public void test() throws Exception {
		TestHttpClient client = app.get("/animal");
		
		
		System.out.println(client.contentsString(1, HOURS));
	}
}
