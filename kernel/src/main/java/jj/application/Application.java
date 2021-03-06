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

import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.Location;
import jj.resource.LocationResolver;
import jj.server.Server;

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
		basePath = arguments.get("app", Path.class, server.resolvePath(Root, "app"));
	}
	
	public Path resolvePath(Location base) {
		assert base instanceof AppLocation;
		AppLocation location = (AppLocation)base;
		return basePath.resolve(location.path()).normalize().toAbsolutePath();
	}

	@Override
	public Path resolvePath(Location base, String name) {
		assert base instanceof AppLocation;
		AppLocation location = (AppLocation)base;
		return basePath.resolve(location.path()).resolve(name).normalize().toAbsolutePath();
	}
	
	@Override
	public Location resolveBase(Path path) {
		assert path != null;
		path = path.normalize().toAbsolutePath();
		Location result = null;
		if (path.startsWith(basePath)) {
			result = AppBase;
			if (path.startsWith(resolvePath(Private))) {
				result = Private;
			} else if (path.startsWith(resolvePath(Public))) {
				result = Public;
			} else if (path.startsWith(resolvePath(Specs))) {
				result = Specs;
			}
		}
		
		return result;
	}
	
	@Override
	public List<Location> watchedLocations() {
		return Collections.singletonList(AppBase);
	}
	
	@Override
	public Location specLocationFor(Location base) {
		return base == Private ? Specs : null;
	}

	public String normalizedName(Location originalBase, Location normalizedBase, String name) {
		assert originalBase == AppBase :
				"originalBase is not AppBase, originalBase: " + originalBase + ", normalizedBase: " + normalizedBase + ", name: " + name;
		assert normalizedBase instanceof AppLocation :
				"normalizedBase is not AppLocation, originalBase: " + originalBase + ", normalizedBase: " + normalizedBase + ", name: " + name;
		AppLocation location = (AppLocation)normalizedBase;
		assert name.startsWith(location.path()) :
				"did not normalize correctly? originalBase: " + originalBase + ", normalizedBase: " + normalizedBase + ", name: " + name;
		return name.substring(location.path().length());
	}
}
