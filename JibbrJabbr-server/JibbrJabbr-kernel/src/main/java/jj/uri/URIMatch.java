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
package jj.uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Simple immutable carrier of a match against a URI, potentially preceded by
 * a SHA key.  determines if the URI is versioned - either a SHA key is the first
 * path particle, or an explicit version is either on the filename or a path
 * preceding it.  Examples of uris that are considered versioned are
 * <ul>
 * <li>/jquery-2.0.2.min.js</li>
 * <li>/jquery-2.0.2.js</li>
 * <li>/fancybox-2.1.5/fancybox.pack.js</li>
 * <li>/fancybox/fancybox-2.1.5.pack.js</li>
 * </ul>
 * 
 * specifically, some series of numbers separated by dots, optionally followed by
 * one of the words 'alpha', 'beta', or 'pre' optionally separated by a dot or a dash,
 * optionally followed by one of the words 'pack' or 'min' separated by a dot or a
 * dash.
 * @author jason
 *
 */
public class URIMatch {
	
	private static final Pattern URI_PATTERN = Pattern.compile("^/(?:([\\da-f]{40})/)?(.*?)(?:\\.([^.]+))?$");
	private static final Pattern VERSION_PATTERN = Pattern.compile("-\\d+(?:[.]\\d+)*(?:[.-]?(?:alpha|beta|pre))?(?:(?:[.-](?:min|pack))?$|/)");

	public final String sha;
	public final String name;
	public final String extension;
	public final String baseName;
	public final boolean versioned;
	
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
		baseName = name == null ? null : name + (extensionCandidate == null ? "" : "." + extensionCandidate);
		versioned = sha != null || (nameCandidate != null && VERSION_PATTERN.matcher(nameCandidate).find());
	}
}
