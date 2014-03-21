package jj.messaging;

import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.configuration.Application;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
class PropertiesResourceCreator extends AbstractResourceCreator<PropertiesResource> {

	private final Application app;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	PropertiesResourceCreator(
		final Application app, 
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.app = app;
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	protected URI uri(AppLocation base, String name, Object... args) {
		return app.resolvePath(base, name).toUri();
	}

	@Override
	public PropertiesResource create(AppLocation base, String name, Object... args) throws IOException {
		return instanceModuleCreator.createResource(PropertiesResource.class, cacheKey(base, name), base, name);
	}

}
