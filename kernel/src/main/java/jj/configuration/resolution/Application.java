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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.Location;
import jj.resource.PathResolver;

/**
 * component for determining app paths, based on the current application root.
 * 
 * @author jason
 *
 */
@Singleton
public class Application implements PathResolver {
	
	private static final String APP_PATH_ARG_NAME = "app";
	
	// TODO source this from the system
	private static final String DEFAULT_APP_PATH = "app";

	private final Arguments arguments;
	
	private final Assets assets;
	
	private final APIModules apiModules;
	
	@Inject
	public Application(final Arguments arguments, final Assets assets, final APIModules apiModules) {
		this.arguments = arguments;
		this.assets = assets;
		this.apiModules = apiModules;
	}

	@Override
	public Location base() {
		return AppLocation.Base;
	}

	@Override
	public Path path() {
		Path result = arguments.get(APP_PATH_ARG_NAME, Path.class);
		if (result == null) result = Paths.get(DEFAULT_APP_PATH);
		return result;
	}

	@Override
	public Path resolvePath(Location base, String name) {
		
		AppLocation location = (AppLocation)base;
		
		switch (location) {
		case Virtual:
			return null;
		case Assets:
			return assets.path(name);
		case APIModules:
			return apiModules.path(name);
		default:
			return path().resolve(location.path()).resolve(name).toAbsolutePath();
		}
	}
}
