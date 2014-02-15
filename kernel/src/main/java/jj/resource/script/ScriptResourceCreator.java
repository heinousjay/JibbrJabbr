package jj.resource.script;

import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.configuration.Application;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class ScriptResourceCreator extends AbstractResourceCreator<ScriptResource> {

	private final Application app;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	ScriptResourceCreator(
		final Application app,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.app = app;
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	public Class<ScriptResource> type() {
		return ScriptResource.class;
	}
	
	@Override
	public boolean canLoad(String name, Object... args) {
		return true;
	}
	
	@Override
	protected URI uri(AppLocation base, String name, Object... args) {
		return app.resolvePath(base, name).toUri();
	}

	@Override
	public ScriptResource create(AppLocation base, String name, Object... args) throws IOException {
		return instanceModuleCreator.createResource(ScriptResource.class, cacheKey(base, name), base, name);
	}

}
