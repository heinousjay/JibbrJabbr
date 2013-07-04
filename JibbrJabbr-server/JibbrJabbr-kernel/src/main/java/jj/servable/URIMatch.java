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
package jj.servable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple immutable carrier of a match against a URI, potentially preceded by
 * a SHA key.
 * @author jason
 *
 */
public class URIMatch {
	
	private static final Pattern URI_PATTERN = Pattern.compile("^/(?:([\\da-f]{40})/)?(.+?)(?:\\.([^.]+))?$");

	public final String sha;
	public final String name;
	public final String extension;
	public final String baseName;
	
	public URIMatch(final String uri) {
		Matcher matcher = URI_PATTERN.matcher(uri);
		String shaCandidate = null;
		String nameCandidate = null;
		String extensionCandidate = null;
		if (matcher.matches()) {
			shaCandidate = matcher.group(1);
			nameCandidate = matcher.group(2);
			extensionCandidate = matcher.group(3);
		}
		sha = shaCandidate;
		name = nameCandidate;
		extension = extensionCandidate;
		baseName = name + (extensionCandidate == null ? "" : "." + extensionCandidate);
	}
}
