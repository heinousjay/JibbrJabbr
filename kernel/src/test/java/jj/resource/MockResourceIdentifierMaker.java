package jj.resource;

import static org.mockito.Mockito.*;

/**
 * @author jason
 */
public class MockResourceIdentifierMaker extends ResourceIdentifierMaker {

	private static PathResolver mockPathResolver() {
		return mock(PathResolver.class); // not even really necessary! but whatever
	}

	public MockResourceIdentifierMaker() {
		super(mockPathResolver());
	}

	@Override
	public <T extends Resource<Void>> ResourceIdentifier<T, Void> make(Class<T> resourceClass, Location base, String name) {
		return make(resourceClass, base, name, null);
	}

	@Override
	public <T extends Resource<A>, A> ResourceIdentifier<T, A> make(Class<T> resourceClass, Location base, String name, A argument) {
		return new ResourceIdentifier<>(resourceClass, base, name, argument);
	}
}
