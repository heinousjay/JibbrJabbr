package jj.resource;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

public class LocationPathResolverBinder {

	private final MapBinder<Class<? extends Location>, PathResolver> pathResolverBinder;
	
	public LocationPathResolverBinder(Binder binder) {
		pathResolverBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends Location>>() {},
			new TypeLiteral<PathResolver>() {}
		);
	}
	
	public interface With {
		void with(Class<? extends PathResolver> pathResolver);
	}
	
	public With resolvePathsFor(Class<? extends Location> locationClass) {
		return (pathResolver) -> {
			pathResolverBinder.addBinding(locationClass).to(pathResolver);
		};
	}
}
