package jj.http.server;

import javax.inject.Singleton;

import jj.application.AppLocation;
import jj.resource.Location;
import jj.server.ServerLocation;

@Singleton
class DefaultRouteProcessorConfiguration implements RouteProcessorConfiguration {

	@Override
	public Location location() {
		return AppLocation.Public.and(ServerLocation.Assets);
	}

}
