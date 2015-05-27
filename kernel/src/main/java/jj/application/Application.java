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
package jj.application;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.Location;
import jj.resource.LocationResolver;
import jj.system.Server;

/**
 * component for determining app paths, based on the current application root.
 * 
 * @author jason
 *
 */
@Singleton
public class Application implements LocationResolver {

	private final Path basePath;
	
	@Inject
	public Application(final Arguments arguments, final Server server) {
		
		basePath = arguments.get("app", Path.class, server.path().resolve("app"));
	}

	@Override
	public Location base() {
		return AppLocation.Base;
	}

	@Override
	public Path path() {
		return basePath;
	}
	
	@Override
	public boolean pathInBase(final Path path) {
		return path.startsWith(basePath);
	}

	@Override
	public Path resolvePath(Location base, String name) {
		assert base instanceof AppLocation;
		AppLocation location = (AppLocation)base;
		return basePath.resolve(location.path()).resolve(name).toAbsolutePath();
	}
}
