package jj.system;

import jj.resource.Location;
import jj.resource.PathResolver;

class SystemLocation implements Location {

	@Override
	public boolean representsFilesystem() {
		return true;
	}

	@Override
	public boolean parentInDirectory() {
		return false; // but soon yes
	}

	@Override
	public PathResolver resolver() {
		return null;
	}
}
