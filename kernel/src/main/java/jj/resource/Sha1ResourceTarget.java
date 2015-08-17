package jj.resource;

// a wrapper to keep guice happy
class Sha1ResourceTarget {
	
	final AbstractFileResource<?> resource;
	
	Sha1ResourceTarget(final AbstractFileResource<?> resource) {
		this.resource = resource;
	}
}