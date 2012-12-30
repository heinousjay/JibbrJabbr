package jj.resource;

public interface ResourceFinder {
	
	<T extends Resource> T findResource(Class<T> resourceClass, String baseName, Object...args);
	
	<T extends Resource> T loadResource(Class<T> resourceClass, String baseName, Object...args);
}
