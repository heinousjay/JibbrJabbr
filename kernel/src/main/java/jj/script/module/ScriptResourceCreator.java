package jj.script.module;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.AbstractResourceCreator;
import jj.resource.Location;
import jj.resource.PathResolver;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class ScriptResourceCreator extends AbstractResourceCreator<ScriptResource> {

	private final PathResolver app;
	private final ResourceInstanceCreator instanceCreator;
	
	@Inject
	ScriptResourceCreator(
		final PathResolver app,
		final ResourceInstanceCreator instanceCreator
	) {
		this.app = app;
		this.instanceCreator = instanceCreator;
	}
	
	@Override
	protected URI uri(Location base, String name, Object... args) {
		return app.resolvePath(base, name).toUri();
	}

	@Override
	public ScriptResource create(Location base, String name, Object... args) throws IOException {
		return instanceCreator.createResource(ScriptResource.class, resourceKey(base, name), base, name);
	}

}
