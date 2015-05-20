package jj.system;

import jj.resource.Location;

enum SystemLocation implements Location {
	
	Root,
	Modules,
	Virtual;

	@Override
	public boolean representsFilesystem() {
		return this != Virtual;
	}

	@Override
	public boolean parentInDirectory() {
		return this == Modules;
	}
}
