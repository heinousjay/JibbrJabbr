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
package jj.script.api;

import java.util.List;

import org.slf4j.Logger;

import jj.logging.Level;
import jj.logging.LoggedEvent;
import jj.logging.NamesLogger;

/**
 * @author jason
 *
 */
class ConsoleLoggingEvent extends LoggedEvent implements NamesLogger {
	
	private static String join(List<String> args) {
		StringBuilder result = new StringBuilder();
		for (String arg : args) {
			result.append(arg).append(" ");
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}
	
	private final String scriptName;
	private final Level level;
	private final List<String> args;
	
	ConsoleLoggingEvent(final String scriptName, final Level level, final List<String> args) {
		this.scriptName = scriptName;
		this.level = level;
		this.args = args;
	}
	
	@Override
	public String loggerName() {
		return "script@" + scriptName;
	}

	@Override
	public void describeTo(Logger logger) {
		switch(level) {
		case Trace:
			if (logger.isTraceEnabled()) {
				logger.trace(join(args));
			}
			break;
			
		case Debug:
			if (logger.isDebugEnabled()) {
				logger.debug(join(args));
			}
			break;
			
		case Info:
			if (logger.isInfoEnabled()) {
				logger.info(join(args));
			}
			break;
			
		case Warn:
			if (logger.isWarnEnabled()) {
				logger.warn(join(args));
			}
			break;
			
		case Error:
			if (logger.isErrorEnabled()) {
				logger.error(join(args));
			}
			break;
		case Off:
		}
	}
}
