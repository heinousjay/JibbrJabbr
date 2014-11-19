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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

/**
 * probably each major subsystem will declare a logger,
 * plus the server core, emergency, test runner 
 * 
 * @author jason
 *
 */
public class LoggingBinder {
	
	private final MapBinder<String, String> loggerNameBinder;
	
	private final MapBinder<Class<? extends Annotation>, Logger> loggerBinder;
	
	public interface BindingBuilder {
		void toLogger(String loggerName);
	}
	
	public LoggingBinder(Binder binder) {
		loggerNameBinder = MapBinder.newMapBinder(binder, String.class, String.class, LoggerNames.class);
		
		loggerBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends Annotation>>() {},
			new TypeLiteral<Logger>() {}
		);
	}
	
	static void registerBuiltins(Binder binder) {
		new LoggingBinder(binder).loggerNameBinder.addBinding("netty").toInstance("io.netty");
	}

	public BindingBuilder annotatedWith(final Class<? extends Annotation> annotation) {
		
		return new BindingBuilder() {
			
			@Override
			public void toLogger(String loggerName) {
				
				if (EmergencyLogger.NAME != loggerName) {
					loggerNameBinder.addBinding(camelCaseName(loggerName)).toInstance(loggerName);
				}
				loggerBinder.addBinding(annotation).toInstance(LoggerFactory.getLogger(loggerName));
			}
		};
	}
	
	private String camelCaseName(String loggerName) {
		
		final StringBuilder output = new StringBuilder(loggerName.length());
		boolean isFirst = true;
		boolean lastWasSpace = false;
		for (char c : loggerName.toCharArray()) {
			if (!Character.isWhitespace(c)) {
				if (lastWasSpace) {
					c = Character.toUpperCase(c);
				}
				assert (!isFirst || Character.isJavaIdentifierStart(c)) && Character.isJavaIdentifierPart(c) :
					"logger names must be composed of characters that are valid in java identifiers and whitespace";
				output.append(c);
			}
			isFirst = false;
			lastWasSpace = Character.isWhitespace(c);
		}
		
		return output.toString();
	}
}
