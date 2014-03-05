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
package jj.webdriver.pages;

import jj.webdriver.By;
import jj.webdriver.Page;
import jj.webdriver.URL;

/**
 * the simple test interface for the page factory.  lives in a different
 * package to ensure that visibility restrictions are tested - PageBase
 * is package private wherever it can be
 * @author jason
 *
 */
@URL("/")
public interface TestPage extends Page {
	
	@By("hi")
	TestPage clickHi();
	
	@By(className = "blast")
	TestPage setBlast(String blast);
	
	@By(id = "something")
	TestPage2 clickSomething();
	
	@By("test-")
	TestPanel testPanel();
	
	@By("best-")
	TestPanel bestPanel();
}