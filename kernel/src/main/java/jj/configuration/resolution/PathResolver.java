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

import jj.configuration.Location;

/**
 * @author jason
 *
 */
public interface PathResolver {
	
	/**
	 * The base location of the application
	 */
	Location base();
	
	/**
	 * The base path of the application
	 */
	Path path();

	/**
	 * resolve the given location and name against
	 * the application base
	 */
	Path resolvePath(Location base, String name);

}