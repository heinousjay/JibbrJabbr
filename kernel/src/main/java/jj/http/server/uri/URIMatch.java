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

import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.util.StringUtils;


/**
 * Simple immutable carrier of a match against a URI, potentially preceded by
 * a hex SHA1 hash.  determines if the URI is versioned - either a hash is
 * the first path particle, or an explicit version is either on the filename
 * or a path preceding it.  Examples of URIs that are considered versioned are
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

	/** the complete URI including any leading and trailing / */
	public final String uri;
	
	/** the resource SHA1 hash, if present */
	public final String sha1;
	
	/** 
	 * the name portion of the uri. this is the portion after any
	 * sha1 hash, without leading or trailing slashes,
	 * and also not including any extensions
	 */
	public final String name;
	
	/**
	 * the extension, as commonly understood - the portion following the last dot in the path,
	 * not including the dot or any trailing slash
	 */
	public final String extension;
	
	/**
	 * the path from the public root, analogous to a resource name,
	 * although route matching rules may virtualize this.
	 * composed of name + '.' + extension.
	 * does not include leading or trailing slashes
	 */
	public final String path;
	
	/**
	 * true if considered to be versioned as detailed in the class comment
	 */
	public final boolean versioned;
	
	public URIMatch(final String uri) {
		assert uri != null : "uri must not be null";
		QueryStringDecoder qsd = new QueryStringDecoder(uri.startsWith("/") ? uri : "/" + uri);
		
		this.uri = "/".equals(uri) ? uri : (Paths.get(qsd.path()).normalize().toString() + (qsd.path().endsWith("/") ? "/" : ""));
		Matcher matcher = URI_PATTERN.matcher(this.uri);
		String shaCandidate = null;
		String nameCandidate = null;
		String extensionCandidate = null;
		if (matcher.matches()) {
			shaCandidate = matcher.group(1);
			nameCandidate = matcher.group(2);
			if (nameCandidate != null && nameCandidate.endsWith("/")) {
				nameCandidate = nameCandidate.substring(0, nameCandidate.length() - 1);
			}
			extensionCandidate = matcher.group(3);
			if (extensionCandidate != null && extensionCandidate.endsWith("/")) {
				extensionCandidate = extensionCandidate.substring(0, extensionCandidate.length() - 1);
			}
		}
		sha1 = shaCandidate;
		name = nameCandidate;
		extension = extensionCandidate;
		path = name == null ? null : name + (extensionCandidate == null ? "" : "." + extensionCandidate);
		versioned = sha1 != null || (nameCandidate != null && VERSION_PATTERN.matcher(nameCandidate).find());
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && other instanceof URIMatch && StringUtils.equals(((URIMatch)other).uri, uri);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{ " +
			"uri: " + uri +
			", sha1: " + sha1 +
			", name: " + name +
			", extension: " + extension + 
			", path: " + path +
			", versioned: " + versioned + " }";
	}
}
