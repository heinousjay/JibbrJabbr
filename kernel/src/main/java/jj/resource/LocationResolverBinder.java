package jj.resource;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

public class LocationResolverBinder {

	private final MapBinder<Class<? extends Location>, LocationResolver> pathResolverBinder;
	
	public LocationResolverBinder(Binder binder) {
		pathResolverBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends Location>>() {},
			new TypeLiteral<LocationResolver>() {}
		);
	}
	
	public interface With {
		void with(Class<? extends LocationResolver> pathResolver);
	}
	
	public With resolvePathsFor(Class<? extends Location> locationClass) {
		return (pathResolver) -> {
			pathResolverBinder.addBinding(locationClass).to(pathResolver);
		};
	}
}
