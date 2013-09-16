package jj.resource.script;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class ScriptResourceCreator extends AbstractResourceCreator<ScriptResource> {

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
	protected Path path(String baseName, Object... args) {
		return configuration.appPath().resolve(baseName);
	}

	@Override
	public ScriptResource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(ScriptResource.class, cacheKey(baseName), baseName, path(baseName));
	}

}
