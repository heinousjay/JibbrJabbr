package jj.resource.script;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class ScriptResourceCreator extends AbstractResourceCreator<ScriptResource> {

	private final Arguments arguments;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	ScriptResourceCreator(
		final Arguments arguments,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.arguments = arguments;
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
	protected URI uri(String baseName, Object... args) {
		return path(baseName).toUri();
	}

	private Path path(String baseName, Object... args) {
		return arguments.appPath().resolve(baseName);
	}

	@Override
	public ScriptResource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(ScriptResource.class, cacheKey(baseName), baseName, path(baseName));
	}

}
