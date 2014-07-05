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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jj.JJ;
import jj.resource.Location;
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
	Virtual("", null, false, false),
	
	/** denotes this asset is a resource located on a path registered with {@link Assets} */
	Assets("", Virtual, false, JJ.jarForClass(AppLocation.class) == null),
	
	/** denotes this asset is a resource located on a path registered with {@link APIModules} */
	APIModules("", Virtual, false, JJ.jarForClass(AppLocation.class) == null),
	
	/** the paths of the application pieces */
	Base("", null, true, true),
	Private("private/", Base, true, true),
	PrivateSpecs("private-specs/", Base, true, true),
	Public("public/", Base, true, true),
	PublicSpecs("public-specs/", Base, true, true);
	
	private final String path;
	private final AppLocation parent;
	private final boolean ensureDirectory;
	private final boolean watchForReloads;
	
	private AppLocation(final String path, final AppLocation parent, boolean ensureDirectory, boolean watchForReloads) {
		this.path = path;
		this.parent = parent;
		this.ensureDirectory = ensureDirectory;
		this.watchForReloads = watchForReloads;
	}
	
	public Location and(Location next) {
		return new Bundle(this, next);
	}
	
	public List<Location> locations() {
		return Collections.unmodifiableList(Arrays.asList((Location)this));
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
	
	@Override
	public boolean parentInDirectory() {
		return ensureDirectory;
	}
	
	@Override
	public boolean watchForReloads() {
		return watchForReloads;
	}
}
