package jj.messaging;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.AbstractResourceCreator;
import jj.resource.Location;
import jj.resource.PathResolver;
import jj.resource.ResourceInstanceCreator;

@Singleton
class PropertiesResourceCreator extends AbstractResourceCreator<PropertiesResource> {

	private final PathResolver app;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	PropertiesResourceCreator(
		final PathResolver app, 
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.app = app;
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	protected URI uri(Location base, String name, Object... args) {
		return app.resolvePath(base, name).toUri();
	}

	@Override
	public PropertiesResource create(Location base, String name, Object... args) throws IOException {
		return instanceModuleCreator.createResource(PropertiesResource.class, resourceKey(base, name), base, name);
	}

}
