package jj.resource;

/**
 * @author jason
 */
public class ResourceIdentifierHelper {

	public static  <T extends Resource<Void>> ResourceIdentifier<T, Void> make(Class<T> resourceClass, Location base, String name) {
		return new ResourceIdentifier<>(resourceClass, base, name, null);
	}

	public static  <A, T extends Resource<A>> ResourceIdentifier<T, A> make(Class<T> resourceClass, Location base, String name, A argument) {
		return new ResourceIdentifier<>(resourceClass, base, name, argument);
	}
}
