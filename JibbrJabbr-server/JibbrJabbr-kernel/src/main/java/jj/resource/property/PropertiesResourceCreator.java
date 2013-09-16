package jj.resource.property;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class PropertiesResourceCreator extends AbstractResourceCreator<PropertiesResource> {

	private final Configuration configuration;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	PropertiesResourceCreator(
		final Configuration configuration, 
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.configuration = configuration;
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
	protected Path path(String baseName, Object... args) {
		return configuration.appPath().resolve(baseName + ".properties");
	}

	@Override
	public PropertiesResource create(String baseName, Object... args) throws IOException {
		return instanceModuleCreator.createResource(PropertiesResource.class, cacheKey(baseName), baseName, path(baseName));
	}

}
