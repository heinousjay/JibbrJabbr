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
package jj;

/**
 * Mainly used to ensure that there is an asynchronous appender
 * in the logging configuration.
 *
 * maybe used to find separate configuration files?
 * 
 * If the kernel detects that logback is in use, then this class
 * is instantiated reflectively.
 * 
 * @author jason
 *
 */
class LogbackConfigurator {

	LogbackConfigurator() {
		// i don't remember how this works
	}
	
}
