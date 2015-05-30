package jj.application;

import jj.JJModule;
import jj.resource.LocationResolverBinder;

public class ApplicationModule extends JJModule {

	@Override
	protected void configure() {
		
		new LocationResolverBinder(binder()).
		// that ugly ugly line!
		resolvePathsFor(AppLocation.class).with(Application.class);
	}

}
