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
import jj.webdriver.WebDriverRule;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class LetsWriteATest {
	
	@Rule
	public JibbrJabbrTestServer server = new JibbrJabbrTestServer(App.two);
	
	
	/** since we're running in the context of the embedded server, let that
	 * produce this rule so they can coordinate. this ensures the server exposes
	 * http, and the webdriver knows the address.
	 * if you want tests that can be moved, you have to supply a url via
	 * {@link WebDriverRule#baseUrl(String)}
	 * TODO - coordinate! logging, server port, startup gating (might need this! jj starts fast though.  maybe it'll be okay)
	 * TODO - make the logging into its own component
	 */
	@Rule
	public WebDriverRule webDriverRule = server.webDriverRule(App.DRIVER_PROVIDER);
	
	@Ignore
	@Test
	public void testChief() throws Exception {
		
		IndexPage index = webDriverRule.get(IndexPage.class);
		
		index.createUserModal().setName("chief").clickUseThisExpectingSuccess();
		
		index.setInput("/bg #5645EF 2500\n")
			.setInput("/topic I AM A CLASSY CHIEF!").clickSay();
		
		//webDriverRule.takeScreenshot();
	}

	@Ignore
	@Test
	public void testBeef() throws Exception {
		
		IndexPage index = webDriverRule.get(IndexPage.class);
		
		index.createUserModal().setName("beef").clickUseThisExpectingSuccess();
		
		index.setInput("/bg #886655 2500\n")
			.setInput("I AM BEEF! I AM KING OF ALL MEATS!").clickSay()
			.setInput("/topic I AM BEEFY BEEF").clickSay();
		
		//webDriverRule.takeScreenshot();
	}
}
