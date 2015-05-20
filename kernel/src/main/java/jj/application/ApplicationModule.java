package jj.application;

import jj.JJModule;
import jj.resource.LocationResolverBinder;
import jj.resource.PathResolver;

public class ApplicationModule extends JJModule {

	@Override
	protected void configure() {
		
		bindAssetPath("/jj/assets");
		
		bindAPIModulePath("/jj/configuration/api");
		
		bind(PathResolver.class).to(Application.class);
		
		new LocationResolverBinder(binder()).
		// that ugly ugly line!
		resolvePathsFor(AppLocation.class).with(Application.class);
	}

}
