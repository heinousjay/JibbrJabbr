package jj.server;

import jj.JJModule;
import jj.resource.LocationResolverBinder;

public class ServerModule extends JJModule {

	@Override
	protected void configure() {
		
		bindAssetPath("/jj/assets");
		
		new LocationResolverBinder(binder()).
		
		resolvePathsFor(ServerLocation.class).with(Server.class);
	}
}
