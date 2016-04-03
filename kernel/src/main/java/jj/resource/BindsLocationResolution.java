package jj.resource;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import jj.HasBinder;

/**
 * Mix-in to help bind location resolution
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsLocationResolution extends HasBinder {

	interface With {
		void with(Class<? extends LocationResolver> pathResolver);
	}

	default With resolvePathsFor(Class<? extends Location> locationClass) {
		return pathResolver ->
			MapBinder.newMapBinder(
				binder(),
				new TypeLiteral<Class<? extends Location>>() {},
				new TypeLiteral<LocationResolver>() {}
			).addBinding(locationClass).to(pathResolver);
	}
}
