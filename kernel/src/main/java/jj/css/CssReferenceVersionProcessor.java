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
package jj.css;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.resolution.AppLocation;
import jj.http.uri.URIMatch;
import jj.resource.PathResolver;
import jj.resource.ResourceFinder;
import jj.resource.ServableResource;
import jj.resource.stat.ic.StaticResource;

/**
 * <p>
 * processes a string in css format to replace internal URL references
 * with versioned URLs
 * 
 * @author jason
 *
 */
@Singleton
class CssReferenceVersionProcessor {

	private static final Pattern IMPORT = Pattern.compile("@import\\s+(['\"])(.+?)\\1");
	private static final Pattern URL = Pattern.compile("url\\((['\"])?(.+?)\\1?\\)");
	private static final Pattern ABSOLUTE = Pattern.compile("^(?:https?:)?//");

	private final PathResolver pathResolver;
	private final ResourceFinder resourceFinder;
	
	@Inject
	CssReferenceVersionProcessor(
		final PathResolver pathResolver,
		final ResourceFinder resourceFinder
	) {
		this.pathResolver = pathResolver;
		this.resourceFinder = resourceFinder;
	}

	
	String fixUris(final String css, final StylesheetResource resource) {
		
		return fixUrls(fixImports(css, resource), resource);
	}
	
	private String fixUrls(final String css, final StylesheetResource resource) {
		return doReplacement(css, resource, URL, "url($1", "$1)", StaticResource.class);
	}
	
	private String fixImports(final String css, final StylesheetResource resource) {
		return doReplacement(css, resource, IMPORT, "@import $1", "$1", StylesheetResource.class);
	}
	
	private String doReplacement(
		final String css,
		final StylesheetResource resource,
		final Pattern pattern,
		final String prefix,
		final String suffix,
		final Class<? extends ServableResource> type
	) {
		// yuck.  the API was never updated
		StringBuffer sb = new StringBuffer();
		
		Matcher matcher = pattern.matcher(css);
		while (matcher.find()) {
			String replacement = matcher.group(2);
			if (!ABSOLUTE.matcher(replacement).find()) {
				
				String name;
				if (replacement.startsWith("/")) {
					name = replacement.substring(1);
				} else {
					name = pathResolver.path()
						.relativize(resource.path().resolveSibling(replacement))
						.normalize()
						.toString();
				
				}
				ServableResource dependency = resourceFinder.loadResource(type, AppLocation.Base, name);
				
				if (dependency != null) {
					dependency.addDependent(resource);
					URIMatch uriMatch = new URIMatch("/" + name);
					if (!uriMatch.versioned) {
						// we only want to replace uris that weren't already versioned
						replacement = dependency.serverPath();
					} else {
						// replace with the absolute path
						replacement = "/" + name;
					}
				}
				
				matcher.appendReplacement(sb, prefix + replacement + suffix);
			}
		}
		matcher.appendTail(sb);
		
		return sb.toString();
	}
}
