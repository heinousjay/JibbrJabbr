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
package jj.logging;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * produces Loggers for a given LoggedEvent descendent
 * 
 * @author jason
 *
 */
@Singleton
class Loggers {

	private final Map<Class<? extends Annotation>, Logger> loggers;
	
	@Inject
	Loggers(final Map<Class<? extends Annotation>, Logger> loggers) {
		this.loggers = loggers;
	}
	
	Logger findLogger(LoggedEvent event) {
		Logger result = null;
		if (event instanceof NamesLogger) {
			result = LoggerFactory.getLogger(((NamesLogger)event).loggerName());
		} else {
			Class<?> eventClass = event.getClass();
			for (Class<? extends Annotation> annotation : loggers.keySet()) {
				if (eventClass.isAnnotationPresent(annotation)) {
					result = loggers.get(annotation);
					break;
				}
			}
		}

		assert result != null : "No logger registered for LoggedEvent " + event;

		return result;
	}
}
