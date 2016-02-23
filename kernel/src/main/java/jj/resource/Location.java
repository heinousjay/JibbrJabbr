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
package jj.resource;

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
		
		Bundle(Location first, Location second) {
			locations.add(first);
			locations.add(second);
		}
		
		@Override
		public Location and(Location next) {
			locations.add(next);
			return this;
		}
		
		@Override
		public List<Location> locations() {
			return Collections.unmodifiableList(locations);
		}
		
		@Override
		public boolean servable() {
			boolean result = true;
			for (Location l : locations) {
				result = result && l.servable();
			}
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Bundle &&
				((Bundle)obj).locations.equals(locations);
		}
		
		@Override
		public boolean parentInDirectory() {
			// bundles cannot be used in this way
			// never should even get called
			throw new AssertionError("called parentInDirectory on a Location.Bundle. should never happen");
		}

		@Override
		public String toString() {
			return locations.toString();
		}
	}

	/**
	 * <p>
	 * Used to chain locations for lookup. The locations are iterated in
	 * the order they are added.
	 * 
	 * <p>
	 * usage:{@code <pre>
	 * FirstLocation.and(SecondLocation).and(ThirdLocation);
	 * </pre>}
	 * 
	 * <p>
	 * The default method is almost certainly what you need, overriding it should 
	 * only be done when you know for sure it's the right thing.
	 */
	default Location and(Location next) {
		return new Bundle(this, next);
	}
	
	/**
	 * <p>
	 * Retrieve the full list of locations
	 * 
	 * <p>
	 * The default method is almost certainly what you need, overriding it should 
	 * only be done when you know for sure it's the right thing.
	 */
	default List<Location> locations() {
		return Collections.singletonList(this);
	}
	
	/**
	 * True if this location can be served to the world, false otherwise
	 * @return
	 */
	boolean servable();
	
	/**
	 * <p>
	 * flag to determine if a resource found in this location should
	 * be parented in a {@link DirectoryResource}
	 */
	boolean parentInDirectory();
}