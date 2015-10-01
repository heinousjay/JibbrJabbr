package jj.resource;

/**
 * Wraps up an abstract file resource meant as a parameter to the Sha1Resource
 */
class Sha1ResourceTarget {
	
	final AbstractFileResource<?> resource;
	
	Sha1ResourceTarget(final AbstractFileResource<?> resource) {
		assert resource != null;
		this.resource = resource;
	}

	@Override
	public String toString() {
		return "targeting " + resource.toString();
	}
}