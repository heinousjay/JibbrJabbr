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
package jj.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ResourceResolver;

/**
 * registration point for asset directories? something like that
 * 
 * @author jason
 *
 */
@Singleton
public class Assets {
	
	public static final String JJ_JS = "jj.js";
	public static final String JQUERY_JS_DEV = "jquery-2.0.3.js";
	public static final String JQUERY_JS = "jquery-2.0.3.min.js";
	public static final String JQUERY_JS_MAP = "jquery-2.0.3.min.map";
	public static final String FAVICON_ICO = "favicon.ico";
	public static final String ERROR_404 = "errors/404.html";
	
	static final Path NOT_FOUND = Paths.get("/jj/assets/not-found-sentinel/");
	
	private final ResourceResolver resolver;
	
	private final Set<String> paths;
	
	@Inject
	Assets(final ResourceResolver resolver, final Set<String> paths) {
		this.resolver = resolver;
		this.paths = paths;
	}

	public Path path(String name) {
		Path result = null;
		try {
			Iterator<String> iterator = paths.iterator();
			while (result == null && iterator.hasNext()) {
				Path path = resolver.pathForFile(iterator.next()).resolve(name);
				if (Files.exists(path)) {
					result = path;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// this is sort of an ugly solution but we have to return a path that doesn't
		// exist so we can be happy
		if (result == null) {
			result = NOT_FOUND;
		}
		
		return result;
	}
}
