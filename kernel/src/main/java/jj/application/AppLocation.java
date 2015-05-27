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
 * <p>
 * This needs a little more work.  The core system should define
 * Virtual and AppBase (and later some system for jars) and those
 * are considered the roots.  All filesystem resources are located
 * under AppBase and can be resolved by appending to that root
 * directly
 * 
 * @author jason
 *
 */
public enum AppLocation implements Location {
	
	/** the paths of the application pieces */
	Base("", null, true, false),
	Private("private/", Base, true, false),
	Specs("specs/", Base, true, false),
	Public("public/", Base, true, false);
	
	private final String path;
	private final AppLocation parent;
	private final boolean ensureDirectory;
	private final boolean internal;
	
	private AppLocation(
		final String path,
		final AppLocation parent,
		final boolean ensureDirectory,
		final boolean internal
	) {
		this.path = path;
		this.parent = parent;
		this.ensureDirectory = ensureDirectory;
		this.internal = internal;
	}
	
	AppLocation parent() {
		return parent;
	}
	
	AppLocation root() {
		return parent == null ? this : parent.root();
	}
	
	String path() {
		return parent == null ? path : parent.path() + path;
	}
	
	public boolean internal() {
		return internal;
	}
	
	@Override
	public boolean parentInDirectory() {
		return ensureDirectory;
	}
}
