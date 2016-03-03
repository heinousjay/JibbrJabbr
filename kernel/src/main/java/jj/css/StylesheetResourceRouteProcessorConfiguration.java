package jj.css;

import javax.inject.Singleton;

import jj.http.server.RouteProcessorConfiguration;
import jj.resource.Location;
import jj.server.ServerLocation;

@Singleton
class StylesheetResourceRouteProcessorConfiguration implements RouteProcessorConfiguration {

	@Override
	public Location location() {
		return ServerLocation.Virtual;
	}

}
