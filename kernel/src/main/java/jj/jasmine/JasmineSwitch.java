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
package jj.jasmine;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;

/**
 * <p>
 * exposes the cli arguments relevant to the jasmine module
 * 
 * <p>
 * runAllSpecs=(boolean)
 * 
 * <p>
 * if true, overrides the autorunSpecs configuration and runs all specs from server
 * start-up.  This option is mainly intended for testing the javascript used during
 * system start-up but it's here if you need it!
 * 
 * @author jason
 *
 */
@Singleton
public class JasmineSwitch {

	private final Arguments arguments;
	
	@Inject
	JasmineSwitch(final Arguments arguments) {
		this.arguments = arguments;
	}
	
	public boolean runAllSpecs() {
		return arguments.get("runAllSpecs", boolean.class, false);
	}
}
