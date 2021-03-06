package jj.engine;

import org.mozilla.javascript.Scriptable;

/**
 * All host objects implement this
 * @author jason
 *
 */
public interface HostObject extends Scriptable {

	String name();
	
	boolean constant();
	
	boolean readonly();
	
	boolean permanent();
	
	boolean dontenum();
}
