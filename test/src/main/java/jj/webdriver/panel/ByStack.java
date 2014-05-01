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
package jj.webdriver.panel;

import jj.webdriver.Panel;

/**
 * maintains the current context of locators for a {@link Panel} hierarchy
 * 
 * @author jason
 *
 */
public class ByStack {
	
	private final String base;
	
	ByStack() {
		base = "";
	}
	
	private ByStack(String base) {
		this.base = base;
	}
	
	ByStack push(String value) {
		
		return new ByStack(base + value);
	}
	
	String resolve(String input) {
		
		return base + input;
	}
}
