package jj.resource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * presents a consistent interface to the notion of resolution of resource
 * names to a {@link Path} if one can be made.  The main motivation is to allow
 * disparate parts of the system to contribute to how resource resolution occurs,
 * but to do so in a fairly controlled manner, in a way that keeps the interface
 * from caring too much about it.
 * @author jason
 *
 */
@Singleton
class PathResolverImpl implements PathResolver {
	
	private final Map<Class<? extends Location>, LocationResolver> resolvers;
	private final List<Location> watchedLocations;

	@Inject
	PathResolverImpl(Map<Class<? extends Location>, LocationResolver> resolvers) {
		this.resolvers = resolvers;
		List<Location> builder = new ArrayList<>();
		for (LocationResolver resolver : resolvers.values()) {
			builder.addAll(resolver.watchedLocations());
		}
		watchedLocations = Collections.unmodifiableList(builder);
	}
	
	@Override
	public Path resolvePath(Location base) {
		return resolvePath(base, "");
	}
	
	@Override
	public Path resolvePath(Location base, String name) {
		
		LocationResolver resolver = resolvers.get(base.getClass());
		
		assert resolver != null : base;
		
		Path result = resolver.resolvePath(base, name);
		
		return result == null ? null : result.normalize().toAbsolutePath();
	}
	
	@Override
	public Location resolveLocation(Path path) {
		Location result = null;
		for (LocationResolver resolver : resolvers.values()) {
			result = resolver.resolveBase(path);
			if (result != null) {
				break;
			}
		}
		return result;
	}
	
	@Override
	public List<Location> watchedLocations() {
		return watchedLocations;
	}
	
	@Override
	public Location specLocationFor(Location base) {

		
		LocationResolver resolver = resolvers.get(base.getClass());
		
		assert resolver != null : base;
		
		return resolver.specLocationFor(base);
	}

	@Override
	public Location normalizedLocation(Location base, String name) {

		LocationResolver resolver = resolvers.get(base.getClass());

		assert resolver != null : base;

		Path path = resolver.resolvePath(base, name);

		return path == null ? base : resolver.resolveBase(path);
	}

	@Override
	public String normalizedName(Location originalBase, Location normalizedBase, String name) {

		assert originalBase.getClass() == normalizedBase.getClass() : "bases need to be the same!";

		LocationResolver resolver = resolvers.get(originalBase.getClass());

		assert resolver != null : originalBase;

		return originalBase == normalizedBase ?
			name :
			resolver.normalizedName(originalBase, normalizedBase, name);
	}
}
