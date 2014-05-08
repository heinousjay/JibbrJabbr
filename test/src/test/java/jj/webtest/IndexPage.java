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

import jj.webdriver.By;
import jj.webdriver.Page;
import jj.webdriver.Panel;
import jj.webdriver.URL;

/**
 * @author jason
 *
 */
@URL("/chat/")
public interface IndexPage extends Page {
	
	public interface CreateUserModal extends Panel {
	
		@By("user-name")
		CreateUserModal setName(String userName);
		
		@By("user-name-form-submit")
		IndexPage clickUseThisExpectingSuccess();
	}
	
	CreateUserModal createUserModal();
	
	@By("userInput")
	IndexPage setInput(String input);
	
	@By("button1")
	IndexPage clickSay();
}
