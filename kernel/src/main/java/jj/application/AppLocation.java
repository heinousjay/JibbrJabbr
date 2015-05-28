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

import jj.resource.Location;

/**
 * <p>
 * Type safe way to represent the locations of resources in the application structure
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
 * @author jason
 *
 */
public enum AppLocation implements Location {
	
	/** the paths of the application pieces */
	Base(""),
	Private("private/"),
	Specs("specs/"),
	Public("public/");
	
	private final String path;
	
	private AppLocation(String path) {
		this.path = path;
	}
	
	String path() {
		return path;
	}
	
	@Override
	public boolean parentInDirectory() {
		return true;
	}
}
