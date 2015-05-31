package jj.server;

import jj.JJModule;
import jj.resource.LocationResolverBinder;

public class ServerModule extends JJModule {

	@Override
	protected void configure() {

		bindAssetPath("/jj/assets");
		bindAPISpecPath("/jj/testing/specs");

		new LocationResolverBinder(binder()).

		resolvePathsFor(ServerLocation.class).with(Server.class);
	}
}
