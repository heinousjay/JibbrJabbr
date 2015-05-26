package jj.system;

import jj.JJModule;
import jj.resource.LocationResolverBinder;

public class SystemModule extends JJModule {

	@Override
	protected void configure() {
		new LocationResolverBinder(binder()).
		
		resolvePathsFor(ServerLocation.class).with(Server.class);
	}
}
