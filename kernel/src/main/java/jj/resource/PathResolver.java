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

import java.nio.file.Path;
import java.util.List;

/**
 * <p>
 * Resolves a {@link Location}/name tuple into a {@link Path},
 * if possible.
 * @author jason
 *
 */
public interface PathResolver {

	Path resolvePath(Location base);
	
	/**
	 * If possible, construct a {@link Path} for the given
	 * {@link Location}/name pair.
	 */
	Path resolvePath(Location base, String name);
	
	List<Location> watchedLocations();
}