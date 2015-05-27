/**
 * <p>
 * Provides a runtime view of the running server, and provides
 * a registration point to handle internal assets and expose API
 * pieces.  Also provides the attach point for modules, and exposes
 * runtime information about the modules that are known to the
 * system.
 * 
 * <p>
 * The main goal is to refactor the kernel into a core and a set
 * of modules that provide higher level functionality.
 * 
 * @author jason
 *
 */
package jj.server;