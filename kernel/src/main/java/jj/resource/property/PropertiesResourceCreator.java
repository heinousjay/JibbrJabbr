package jj.resource.property;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class PropertiesResourceCreator extends AbstractResourceCreator<PropertiesResource> {

	private final Arguments arguments;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	PropertiesResourceCreator(
		final Arguments arguments, 
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.arguments = arguments;
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	public Class<PropertiesResource> type() {
		return PropertiesResource.class;
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
		return arguments.appPath().resolve(baseName + ".properties");
	}

	@Override
	public PropertiesResource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(PropertiesResource.class, cacheKey(baseName), baseName, path(baseName));
	}

}
