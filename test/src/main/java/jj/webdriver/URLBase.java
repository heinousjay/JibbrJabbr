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
package jj.webdriver;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

/**
 * Represents and calculates actual navigation URLs.
 * it's actually just a string appender! but it's
 * a simple way to wrap up the details
 * 
 * @author jason
 *
 */
@Singleton
class URLBase {
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	@Target(PARAMETER)
	@interface BaseURL {}

	private final String urlBase;
	
	@Inject
	URLBase(final @BaseURL String urlBase) {
		this.urlBase = urlBase;
	}
	
	String resolve(String input) {
		return urlBase + input;
	}
}
