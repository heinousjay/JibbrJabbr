package jj.server;

import jj.resource.Location;
import jj.script.ScriptEnvironment;

/**
 * <p>
 * Typesafe representation of the structure of the server's
 * resources.
 * 
 * @author jason
 *
 */
public enum ServerLocation implements Location {
	
	/**
	 * The root of the server.  In most cases, this will
	 * be the directory of the executable, although it
	 * can be changed with arguments.
	 */
	Root,
	
	/**
	 * A subdirectory of the root named "modules" which contains
	 * modules, which are explained in {@link ModuleResource}.
	 */
	Modules,
	
	/**
	 * The home for resources that do not exist in the filesystem
	 * or in any other place. For example, {@link ScriptEnvironment}s.
	 */
	Virtual,
	
	/**
	 * Resources backed by files located on the classpath of
	 * the running server, registered in a module, and intended
	 * for serving to the outside world.
	 */
	Assets,
	
	/**
	 * Resources backed by files located on the classpath of
	 * the running server, registered in a module, and intended
	 * for use in scripts via the require mechanism.
	 */
	APIModules,
	
	/**
	 * Specs to validate API modules
	 */
	APISpecs;

	@Override
	public boolean parentInDirectory() {
		return this == Modules;
	}
	
	@Override
	public boolean servable() {
		return this == Assets;
	}
}
