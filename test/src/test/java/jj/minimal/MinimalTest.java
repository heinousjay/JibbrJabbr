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
package jj.minimal;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import jj.App;
import jj.testing.JibbrJabbrTestServer;
import jj.webdriver.PhantomJSWebDriverProvider;
import jj.webdriver.WebDriverRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * Just validates the minimal app
 * 
 * @author jason
 *
 */
public class MinimalTest {
	
	@Rule
	public JibbrJabbrTestServer jibbrjabbr = new JibbrJabbrTestServer(App.minimal);
	
	// asking for a WebDriverRule from the server rule automatically configures the
	// server to start listening on 8080, and configures the rules to a base url of
	// "http://localhost:8080"
	// this behavior has some nuances i haven't explained here but it's all thought out
	
	@Rule
	public WebDriverRule web1 = jibbrjabbr.webDriverRule()
		.driverProvider(PhantomJSWebDriverProvider.class);
	
	// need two browsers?  make two web driver rules! yay!
	
	@Rule
	public WebDriverRule web2 = jibbrjabbr.webDriverRule()
		// kinda ugly supplying this twice, but all i can 
		// think of is passing it twice into the factory anyway
		.driverProvider(PhantomJSWebDriverProvider.class);

	@Test
	public void test() throws Exception {
		
		IndexPage one = web1.get(IndexPage.class);
		IndexPage two = web2.get(IndexPage.class);
		
		one.setSay("something").clickSubmit();
		two.setSay("otherthing").clickSubmit();
		
		assertThat(one.readLine(1), is("something"));
		assertThat(two.readLine(1), is("something"));
		assertThat(one.readLine(2), is("otherthing"));
		assertThat(two.readLine(2), is("otherthing"));
	}

}
