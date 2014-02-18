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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * component for determining app paths, based on the current application root.
 * 
 * @author jason
 *
 */
@Singleton
public class Application {
	
	private static final String APP_PATH_ARG_NAME = "app";
	
	// TODO source this from the system
	private static final String DEFAULT_APP_PATH = "app";

	private final Arguments arguments;
	
	private final Assets assets;
	
	@Inject
	public Application(final Arguments arguments, final Assets assets) {
		this.arguments = arguments;
		this.assets = assets;
	}
	
	public Path configPath() {
		return path().resolve("config.js");
	}
	
	public Path path() {
		Path result = arguments.get(APP_PATH_ARG_NAME, Path.class);
		if (result == null) result = Paths.get(DEFAULT_APP_PATH);
		return result;
	}

	/**
	 * @param base
	 * @param name
	 * @return
	 */
	public Path resolvePath(AppLocation base, String name) {
		
		switch (base) {
		case Virtual:
			return null;
		case Assets:
			return assets.path(name);
		default:
			return path().resolve(base.path()).resolve(name);
		}
	}
}