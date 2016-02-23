package jj.resource;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handles creation of resource identifiers, including normalizing
 * locations
 *
 * @author jason
 */
@Singleton
public class ResourceIdentifierMaker {

	private final PathResolver pathResolver;

	@Inject
	ResourceIdentifierMaker(PathResolver pathResolver) {
		this.pathResolver = pathResolver;
	}

	public <T extends Resource<Void>> ResourceIdentifier<T, Void> make(Class<T> resourceClass, Location base, String name) {
		return make(resourceClass, base, name, null);
	}

	public <T extends Resource<A>, A> ResourceIdentifier<T, A> make(Class<T> resourceClass, Location base, String name, A argument) {
		// find the canonical tuple
		Location normalizedBase = base instanceof Location.Bundle ? base : pathResolver.normalizedLocation(base, name);
		if (normalizedBase != null && !normalizedBase.equals(base)) {
			name = pathResolver.normalizedName(base, normalizedBase, name);
		}

		return new ResourceIdentifier<>(resourceClass, base, name, argument);
	}
}
