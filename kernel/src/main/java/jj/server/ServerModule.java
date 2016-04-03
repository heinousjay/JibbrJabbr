package jj.server;

import jj.JJModule;
import jj.resource.BindsLocationResolution;

public class ServerModule extends JJModule implements BindsLocationResolution, BindsServerPath {

	@Override
	protected void configure() {

		bindAssetPath("/jj/assets");
		bindAPISpecPath("/jj/testing/specs");

		resolvePathsFor(ServerLocation.class).with(Server.class);
	}
}
