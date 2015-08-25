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
package jj.http.server.uri;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <p>
 * Inspects a route URI string to validate that it is legal
 * <ol>
 *  <li>Starts with /
 *  <li>is composed of segments separated by /
 *  <li>segments match either
 * </ol>
 * 
 * <p>
 * This complicates the usage a little bit, since in principle you should
 * call the validator before creating a route, but it's set up this way
 * for the javascript API, because throwing exceptions makes that all
 * weird and hard to write
 * @author jason
 *
 */
@Singleton
public class RouteUriValidator {
	
	private static final Pattern SPLITTER = Pattern.compile("/");
	
	static final Pattern STATIC_NODE_PATTERN = Pattern.compile("^[a-zA-Z0-9.\\+%-]*$");
	
	static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("^[:\\*][a-zA-Z$_][a-zA-Z0-9$_]*$");
	

	@Inject
	RouteUriValidator() {}
	
	private void append(StringBuilder errorBuilder, String...errors) {
		
		if (errorBuilder.length() > 0) {
			errorBuilder.append("\n");
		}
		
		for (String error : errors) {
			errorBuilder.append(error);
		}
	}
	
	private void validateStaticSegment(String segment, StringBuilder errors) {
		if (!STATIC_NODE_PATTERN.matcher(segment).matches()) {
			append(errors, "segment ", segment, " contains invalid URL characters");
		}
	}
	
	private void validateParameter(String segment, StringBuilder errors) {
		int patternStart = segment.indexOf('(');
		String pattern = "";
		String toCheck = segment;
		if (patternStart > -1) {
			pattern = segment.substring(patternStart);
			toCheck = segment.substring(0, patternStart);
		}
		
		if (!PARAMETER_NAME_PATTERN.matcher(toCheck).matches()) {
			append(errors, "parameter ", segment, " must have a valid JavaScript variable name");
		}
		if (!pattern.isEmpty()) {
			if (!pattern.endsWith(")")) {
				append(errors, "parameter ", segment, " has an invalid pattern specification ", pattern);
			} else {
				try {
					Pattern.compile(pattern.substring(1, pattern.length() - 1));
				} catch (PatternSyntaxException pse) {
					append(errors, "parameter ", segment ," pattern ", pattern.substring(1, pattern.length() - 1), " failed to compile");
					append(errors, pse.getMessage());
				}
			}
		}
	}
	
	private void validateParamSegment(String segment, StringBuilder errors) {
		validateParameter(segment, errors);
	}
	
	private void validateSplatSegment(String segment, boolean last, StringBuilder errors) {
		if (!last) {
			errors.append("* parameter must be the last path segment in a uri");
		}
		validateParameter(segment, errors);
	}
	
	private void doValidateSegment(String segment, boolean last, StringBuilder errors) {
		if (segment.startsWith("*")) {
			validateSplatSegment(segment, last, errors);
		} else if (segment.startsWith(":")) {
			validateParamSegment(segment, errors);
		} else {
			validateStaticSegment(segment, errors);
		}
	}
	
	private void validateSegment(String segment, boolean last, StringBuilder errors) {
		String toValidate = segment;
		String extension = null;
		if (last) {
			// split off any extension found
			int end = segment.lastIndexOf('.');
			if (end > -1) {
				toValidate = segment.substring(0, end);
				extension = segment.substring(end + 1);
			}
		}
		
		doValidateSegment(toValidate, last, errors);
		if (extension != null) {
			doValidateSegment(extension, last, errors);
		}
	}
	
	private void validate(String[] segments, StringBuilder errors) {
		
		for (int i = 0; i < segments.length; ++i) {
			validateSegment(segments[i], i == segments.length - 1, errors);
		}
	}
	
	/**
	 * Validates a route URI, returning as many errors as can be discovered 
	 * as a \n separated string.  if the returned string is empty, it's all
	 * good
	 */
	public String validateRouteUri(final String uri) {
		
		StringBuilder errors = new StringBuilder();
		
		if (uri == null) {
			append(errors, "uri must not be null");
		}
		
		else if (uri.isEmpty() || uri.charAt(0) != '/') {
			append(errors, "uri must start with /");
			validate(SPLITTER.split(uri), errors);
		}
		
		else {
			validate(SPLITTER.split(uri.substring(1)), errors);
		}
		
		return errors.toString();
	}
}
