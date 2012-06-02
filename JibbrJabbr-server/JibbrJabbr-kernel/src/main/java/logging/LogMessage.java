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
package logging;

import org.slf4j.Logger;
import org.slf4j.Marker;

import net.jcip.annotations.Immutable;

/**
 * just used to pass the log message through the queue
 * 
 * @author jason
 *
 */
@Immutable
final class LogMessage {
	
	enum Type {
		Trace {
			void log(final Logger logger, final Marker marker, final String message, final Object[] args) {
				logger.trace(marker, message, args);
			}
		},
		Debug {
			void log(final Logger logger, final Marker marker, final String message, final Object[] args) {
				logger.debug(marker, message, args);
			}
		},
		Info {
			void log(final Logger logger, final Marker marker, final String message, final Object[] args) {
				logger.info(marker, message, args);
			}
		},
		Warn {
			void log(final Logger logger, final Marker marker, final String message, final Object[] args) {
				logger.warn(marker, message, args);
			}
		},
		Error {
			void log(final Logger logger, final Marker marker, final String message, final Object[] args) {
				logger.error(marker, message, args);
			}
		};
		
		abstract void log(final Logger logger, final Marker marker, final String message, final Object[] args);
	}
	
	final String loggerName;
	final Marker marker;
	final Type type;
	final String message;
	final Object[] args;
	
	LogMessage(final String loggerName, final Marker marker, final Type type, final String message, final Object[] args) {
		this.loggerName = loggerName;
		this.marker = marker;
		this.type = type;
		this.message = message;
		this.args = args;
	}

}
