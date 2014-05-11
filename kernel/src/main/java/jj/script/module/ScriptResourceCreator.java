package jj.script.module;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Location;
import jj.configuration.resolution.PathResolver;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class ScriptResourceCreator extends AbstractResourceCreator<ScriptResource> {

	private final PathResolver app;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	ScriptResourceCreator(
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
	public ScriptResource create(Location base, String name, Object... args) throws IOException {
		return instanceModuleCreator.createResource(ScriptResource.class, resourceKey(base, name), base, name);
	}

}
