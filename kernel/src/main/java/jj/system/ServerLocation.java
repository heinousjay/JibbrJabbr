package jj.system;

import jj.resource.Location;

public enum ServerLocation implements Location {
	
	Root,
	Modules,
	Virtual;

	@Override
	public boolean parentInDirectory() {
		return this == Modules;
	}
}
