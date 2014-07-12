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

import java.util.Collection;

import org.slf4j.Logger;

import jj.ServerLogger;
import jj.logging.LoggedEvent;

/**
 * @author jason
 *
 */
@ServerLogger
public abstract class JasmineTestResult extends LoggedEvent {
	
	protected final Collection<Suite> suites;
	
	JasmineTestResult(final Collection<Suite> suites) {
		this.suites = suites;
	}
	
	protected abstract String description();
	
	@Override
	public void describeTo(Logger logger) {
		if (logger.isInfoEnabled()) {
			StringBuilder result = new StringBuilder();
			for (Suite suite : suites) {
				result.append("\n").append(suite);
			}
			
			logger.info(description() + "\n{}", result);
		}
	}

}
