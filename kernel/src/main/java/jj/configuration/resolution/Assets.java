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
package jj.configuration.resolution;

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
public class Assets extends InternalAssets {
	
	// these are kinda goofy?
	public static final String JJ_JS = "jj.js";
	public static final String JQUERY_JS_DEV = "jquery-2.0.3.js";
	public static final String JQUERY_JS = "jquery-2.0.3.min.js";
	public static final String JQUERY_JS_MAP = "jquery-2.0.3.min.map";
	public static final String FAVICON_ICO = "favicon.ico";
	public static final String ERROR_404 = "errors/404.html";
	
	@Inject
	protected Assets(final ResourceResolver resolver, final @AssetPaths Set<String> paths) {
		super(resolver, paths);
	}
}
