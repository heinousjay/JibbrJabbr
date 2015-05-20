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

import jj.resource.Location;
import jj.resource.PathResolver;
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
	
	/** 
	 * denotes this resource is not from the application file system,
	 * such as a {@link ScriptEnvironment} 
	 */
	Virtual("", null, false, false, false),
	
	/** denotes this asset is a resource located on a path registered with {@link Assets} */
	Assets("", Virtual, false, true, true),
	
	/** denotes this asset is a resource located on a path registered with {@link APIModules} */
	APIModules("", Virtual, false, true, true),
	
	/** the paths of the application pieces */
	Base("", null, true, true, false),
	Private("private/", Base, true, true, false),
	PrivateSpecs("private-specs/", Base, true, true, false),
	Public("public/", Base, true, true, false),
	PublicSpecs("public-specs/", Base, true, true, false);
	
	private final String path;
	private final AppLocation parent;
	private final boolean ensureDirectory;
	private final boolean representsFilesystem;
	private final boolean internal;
	
	private AppLocation(final String path, final AppLocation parent, final boolean ensureDirectory, final boolean representsFilesystem, final boolean internal) {
		this.path = path;
		this.parent = parent;
		this.ensureDirectory = ensureDirectory;
		this.representsFilesystem = representsFilesystem;
		this.internal = internal;
	}
	
	public Location parent() {
		return parent;
	}
	
	public Location root() {
		return parent == null ? this : parent.root();
	}
	
	public String path() {
		return parent == null ? path : parent.path() + path;
	}
	
	public boolean internal() {
		return internal;
	}
	
	@Override
	public boolean parentInDirectory() {
		return ensureDirectory;
	}
	
	@Override
	public boolean representsFilesystem() {
		return representsFilesystem;
	}
	
	@Override
	public PathResolver resolver() {
		return null;
	}
}
