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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstraction of a location of a resource
 * @author jason
 *
 */
public interface Location {
	
	public static class Bundle implements Location {
		
		private final List<Location> locations = new ArrayList<>();
		
		public Bundle(Location first, Location second) {
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
			return obj instanceof Bundle &&
				((Bundle)obj).locations.equals(locations);
		}
		
		@Override
		public Location parent() {
			// bundles never have a parent, and
			// this should never be called on a bundle
			throw new AssertionError("called parent on a Location.Bundle. should never happen");
		}
		
		@Override
		public Location root() {
			// bundles never have a parent, and
			// this should never be called on a bundle
			throw new AssertionError("called root on a Location.Bundle. should never happen");
		}
		
		@Override
		public boolean parentInDirectory() {
			// bundles cannot be used in this way
			// never should even get called
			throw new AssertionError("called directoryParent on a Location.Bundle. should never happen");
		}
	}
	
	Location and(Location location);
	
	List<Location> locations();
	
	Location parent();
	
	Location root();
	
	boolean parentInDirectory();
}