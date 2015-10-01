package jj.resource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * central lookup for all resources, mapped from the URI
 * representation of their path to the resource.
 * @author jason
 *
 */
@Singleton
@Subscriber
class ResourceCache {
	
	private final ResourceCreators resourceCreators;
	
	private final ConcurrentMap<ResourceIdentifier, Resource<?>> resourceCache = new ConcurrentHashMap<>(128, 0.75F, 4);

	@Inject
	ResourceCache(final ResourceCreators resourceCreators) {
		this.resourceCreators = resourceCreators;
	}

	List<Resource<?>> findAllByPath(Path path) {
		return Collections.unmodifiableList(resourceCache.values().stream().filter(
			resource ->
				resource.alive() && // skip the deaders!
				resource instanceof FileSystemResource && // and check for path equality
				path.equals(((FileSystemResource)resource).path())
		).collect(Collectors.toList()));
	}
	
	/**
	 * Returns an unmodifiable snapshot of the current Resource instances.  This information is
	 * immediately out of date, cannot be manipulated, and is in no particular order.
	 */
	List<Resource<?>> allResources() {
		return Collections.unmodifiableList(new ArrayList<>(resourceCache.values()));
	}
	
	@Listener
	void on(ServerStopping event) {
		resourceCache.clear();
	}
	
	@SuppressWarnings("unchecked")
	<A, T extends Resource<A>> ResourceCreator<A, T> getCreator(final Class<T> type) {
		return (ResourceCreator<A, T>) resourceCreators.get(type);
	}

	<T extends Resource<A>, A> T get(ResourceIdentifier<T, A> identifier) {
		return identifier.resourceClass.cast(resourceCache.get(identifier));
	}

	<T extends Resource<A>, A> T putIfAbsent(T resource) {
		@SuppressWarnings("unchecked")
		ResourceIdentifier<T, A> identifier = (ResourceIdentifier<T, A>) resource.identifier();
		return identifier.resourceClass.cast(resourceCache.putIfAbsent(identifier, resource));
	}

	<T extends Resource<?>> boolean replace(Resource<T> newResource) {
		return resourceCache.replace(newResource.identifier(), newResource) != null;
	}

	<A, T extends Resource<A>> boolean replace(Resource<T> currentResource, Resource<T> newResource) {
		ResourceIdentifier<? ,?> identifier = currentResource.identifier();
		assert identifier.equals(newResource.identifier()) : "RESOURCE REPLACEMENT MUST BE EQUIVALENT";
		return resourceCache.replace(identifier, currentResource, newResource);
	}

	<A, T extends Resource<A>> boolean remove(T resource) {
		return resourceCache.remove(resource.identifier(), resource);
	}
	
	int size() {
		return resourceCache.size();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName()).append(" {\n");
		resourceCache.forEach((identifier, resource) -> {
			sb.append("  ").append(identifier).append(" = ").append(resource).append("\n");
		});
 		return sb.append("}").toString();
	}
}
