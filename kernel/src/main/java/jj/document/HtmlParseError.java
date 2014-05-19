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
package jj.document;

import java.nio.file.Path;
import java.util.List;

import org.jsoup.parser.ParseError;
import org.slf4j.Logger;

import jj.logging.LoggedEvent;

/**
 * @author jason
 *
 */
public class HtmlParseError extends LoggedEvent {

	private final Path path;
	private final List<ParseError> errors;
	
	public HtmlParseError(final Path path, final List<ParseError> errors) {
		this.path = path;
		this.errors = errors;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.warn("errors while parsing {}, your document may not behave as expected", path);
		
		for (ParseError pe : errors) {
			logger.warn("{}", pe);
		}
		errors.clear();
	}

}
