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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jj.configuration.Location;
import jj.script.ScriptEnvironment;

/**
 * <p>
 * Type safe way to represent the locations of resources, either in the application structure
 * </p>
 * 
 * <pre>
 * (root location)
 * |-config.js
 * \--private
 *  |-private-specs
 *  |-public
 *  |-public-specs
 * </pre>
 * 
 * <p>or internally with virtual
 * 
 * @author jason
 *
 */
public enum AppLocation implements Location {
	
	/** denotes this resource is not from the file system, such as a {@link ScriptEnvironment} */
	Virtual(""),
	
	/** denotes this asset is a resource located on a path registered with {@link Assets} */
	Assets(""),
	
	/** denotes this asset is a resource located on a path registered with {@link APIModules} */
	APIModules(""),
	
	/** the paths of the application pieces */
	Base(""),
	Private("private"),
	PrivateSpecs("private-specs"),
	Public("public"),
	PublicSpecs("public-specs");
	
	private final String path;
	
	private AppLocation(final String path) {
		this.path = path;
	}
	
	public final class AppLocationBundle implements Location {
		private final List<Location> locations = new ArrayList<>();
		
		AppLocationBundle(Location first, Location second) {
			locations.add(first);
			locations.add(second);
		}
		
		public Location and(Location next) {
			locations.add(next);
			return this;
		}
		
		public List<Location> locations() {
			return Collections.unmodifiableList(locations);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof AppLocationBundle &&
				((AppLocationBundle)obj).locations.equals(locations);
		}
	}
	
	public Location and(Location next) {
		return new AppLocationBundle(this, next);
	}
	
	public List<Location> locations() {
		return Collections.unmodifiableList(Arrays.asList((Location)this));
	}
	
	public String path() {
		return path;
	}
}
