package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.CoreConfiguration;
import jj.configuration.Configuration;

@Singleton
class ScriptResourceCreator extends AbstractResourceCreator<ScriptResource> {

	private final Configuration configuration;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	ScriptResourceCreator(
		final Configuration configuration,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.configuration = configuration;
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
	Path path(String baseName, Object... args) {
		return configuration.get(CoreConfiguration.class).appPath().resolve(baseName);
	}

	@Override
	public ScriptResource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(ScriptResource.class, cacheKey(baseName), baseName, path(baseName));
	}

}
