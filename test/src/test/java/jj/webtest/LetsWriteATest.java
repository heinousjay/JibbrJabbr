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
package jj.webtest;

import jj.App;
import jj.testing.JibbrJabbrTestServer;
import jj.webdriver.FirefoxWebDriverProvider;
import jj.webdriver.PhantomJSWebDriverProvider;
import jj.webdriver.WebDriverRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class LetsWriteATest {
	
	@Rule
	public JibbrJabbrTestServer server = 
		new JibbrJabbrTestServer(App.path2)
			.withHttp();
	
	
	// since we're running in the context of the embedded server, let that
	// produce this rule so they can coordinate
	// TODO - coordinate! logging, server port, startup gating (ensure the server is running first!)
	// TODO - make the logging into its own component
	@Rule
	public WebDriverRule webDriverRule = server.webDriverRule()
	
		// normally you would be sourcing this from some project-specific configuration
		// but for the purposes of testing the rule, we specifically want this
		
		.driverProvider(PhantomJSWebDriverProvider.class)
		//.driverProvider(FirefoxWebDriverProvider.class)
		
		// similarly for this parameter. typically you'll be sourcing this
		// from some project-specific system properties, but for now this is fine
		// TODO - configure this automatically from the test server rule.
		// but for now i am tired
		
		.baseUrl("http://localhost:8080");
		//.baseUrl("https://jibbrjabbr.com");

	@Test
	public void test() throws Exception {
		
		IndexPage index = webDriverRule.get(IndexPage.class);
		
		index.setUserName("chief").clickUseThis();
		
		index.setInput("/bg #5645EF 2500\n")
			.setInput("/topic I AM A CLASSY CHIEF!").clickSay();
		
		webDriverRule.takeScreenshot();
	}

	@Test
	public void test2() throws Exception {
		
		IndexPage index = webDriverRule.get(IndexPage.class);
		
		index.setUserName("beef").clickUseThis();
		
		index.setInput("/bg #886655 2500\n")
			.setInput("I AM BEEF! I AM KING OF ALL MEATS!").clickSay()
			.setInput("/topic I AM BEEFY BEEF").clickSay();
		
		webDriverRule.takeScreenshot();
	}
}
