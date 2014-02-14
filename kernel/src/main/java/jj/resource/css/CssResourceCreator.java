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
package jj.resource.css;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import jj.SHA1Helper;
import jj.configuration.Arguments;
import jj.logging.EmergencyLogger;
import jj.resource.AbstractResourceCreator;
import jj.resource.Resource;
import jj.resource.ResourceFinder;
import jj.resource.ResourceInstanceCreator;
import jj.resource.stat.ic.StaticResource;
import jj.uri.URIMatch;

/**
 * @author jason
 *
 */
@Singleton
public class CssResourceCreator extends AbstractResourceCreator<CssResource> {
	
	private static final Pattern DOT_CSS = Pattern.compile("\\.css$");
	private static final Pattern DOT_LESS = Pattern.compile("\\.less$");
	
	private static final Pattern IMPORT = Pattern.compile("@import\\s+(['\"])(.+?)\\1");
	private static final Pattern URL = Pattern.compile("url\\((['\"])?(.+?)\\1?\\)");
	private static final Pattern ABSOLUTE = Pattern.compile("^(?:https?:)?//");
	
	private final Arguments arguments;
	private final LessProcessor lessProcessor;
	private final ResourceFinder resourceFinder;
	private final Logger logger;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	CssResourceCreator(
		final Arguments arguments,
		final LessProcessor lessProcessor,
		final ResourceFinder resourceFinder,
		final @EmergencyLogger Logger logger,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.arguments = arguments;
		this.lessProcessor = lessProcessor;
		this.resourceFinder = resourceFinder;
		this.logger = logger;
		this.instanceModuleCreator = instanceModuleCreator;
	}

	@Override
	public Class<CssResource> type() {
		return CssResource.class;
	}
	
	private String toLess(final String baseName) {
		return DOT_CSS.matcher(baseName).replaceFirst(".less");
	}
	
	@Override
	public boolean canLoad(String name, Object... args) {
		return (args.length == 1 && Boolean.TRUE.equals(args[0])) ?
			DOT_LESS.matcher(name).find() :
			DOT_CSS.matcher(name).find();
	}
	
	@Override
	protected URI uri(String baseName, Object... args) {
		return path(baseName, args).toUri();
	}
	
	/**
	 * takes one parameter, if Boolean.TRUE then this tries to load a .less file
	 */
	private Path path(final String baseName, Object... args) {
		
		if (args != null && args.length == 1 && Boolean.TRUE.equals(args[0])) {
			return arguments.appPath().resolve(toLess(baseName));
			
		}
		return arguments.appPath().resolve(baseName);
	}

	@Override
	public CssResource create(String baseName, Object... args) throws IOException {
		boolean less = args.length == 1 && Boolean.TRUE.equals(args[0]);
		
		CssResource resource = null;
		if (Files.exists(path(baseName, less))) {
		
			resource = instanceModuleCreator.createResource(
				CssResource.class,
				cacheKey(baseName, less),
				baseName,
				path(baseName, less),
				less
			);
			
			String processed = fixUris(
				less ? lessProcessor.process(toLess(baseName)) : resource.byteBuffer.toString(UTF_8),
				resource
			);
			resource.byteBuffer.clear().writeBytes(processed.getBytes(UTF_8));
			resource.sha1(SHA1Helper.keyFor(resource.byteBuffer));
		}
		return resource;
	}
	
	private String fixUris(final String css, final CssResource resource) {
		
		return fixUrls(fixImports(css, resource), resource);
	}
	
	private String fixUrls(final String css, final CssResource resource) {
		return doReplacement(css, resource, URL, "url($1", "$1)", StaticResource.class);
	}
	
	private String fixImports(final String css, final CssResource resource) {
		// TODO account for less resources (not sure what i meant by this - i think i already did?)
		// TODO write a test that ensures we are accounting for less resources, if that isn't already happening
		return doReplacement(css, resource, IMPORT, "@import $1", "$1", CssResource.class);
	}
	
	private String doReplacement(
		final String css,
		final CssResource resource,
		final Pattern pattern,
		final String prefix,
		final String suffix,
		final Class<? extends Resource> type
	) {
		// yuck.  the API was never updated
		StringBuffer sb = new StringBuffer();
		
		Matcher matcher = pattern.matcher(css);
		while (matcher.find()) {
			String replacement = matcher.group(2);
			if (!ABSOLUTE.matcher(replacement).find()) {
				
				String baseName;
				if (replacement.startsWith("/")) {
					baseName = replacement.substring(1);
				} else {
					baseName = 
						arguments
							.appPath()
							.relativize(resource.path().resolveSibling(replacement))
							.normalize()
							.toString();
				
				}
				Resource dependency = resourceFinder.loadResource(type, baseName);
				
				if (dependency != null) {
					dependency.addDependent(resource);
					URIMatch uriMatch = new URIMatch("/" + baseName);
					if (!uriMatch.versioned) {
						// we only want to replace uris that weren't already versioned
						replacement = dependency.uri();
					} else {
						// replace with the absolute path
						replacement = "/" + baseName;
					}
				} else {
					logger.warn("CSS file {} references {} (as {}), which does not exist", resource.baseName(), baseName, matcher.group());
				}
				
				matcher.appendReplacement(sb, prefix + replacement + suffix);
			}
		}
		matcher.appendTail(sb);
		
		return sb.toString();
	}
}
