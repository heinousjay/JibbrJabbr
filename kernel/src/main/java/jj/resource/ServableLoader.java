package jj.resource;

import static io.netty.handler.codec.http.HttpMethod.GET;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.ServableResource;
import jj.http.server.ServableResources;
import jj.http.server.uri.RouteMatch;
import jj.http.server.uri.Router;
import jj.http.server.uri.URIMatch;

@Singleton
public class ServableLoader {
	
	private final ServableResources servables;
	private final Router router;
	
	@Inject
	ServableLoader(
		final ServableResources servables,
		final Router router
	) {
		this.servables = servables;
		this.router = router;
	}

	public ServableResource loadResource(URIMatch uriMatch) {
		ServableResource resource = null;
		if (uriMatch.path != null) {
			
			RouteMatch match = router.routeRequest(GET, uriMatch);
			if (match.matched()) {
				Class<? extends ServableResource> resourceClass = servables.classFor(match.resourceName());
				resource = servables.routeProcessor(match.resourceName()).loadResource(resourceClass, uriMatch, match.route());
			}
		}
		return resource;
	}
}
