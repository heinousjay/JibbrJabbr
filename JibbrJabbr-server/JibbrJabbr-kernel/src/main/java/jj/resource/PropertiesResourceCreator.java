package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;

@Singleton
class PropertiesResourceCreator implements ResourceCreator<PropertiesResource> {

	private final URI baseUri;
	private final Path basePath;
	
	@Inject
	PropertiesResourceCreator(final Configuration configuration) {
		this.baseUri = configuration.baseUri();
		this.basePath = configuration.basePath();
	}
	
	@Override
	public Class<PropertiesResource> type() {
		return PropertiesResource.class;
	}

	/**
	 * the args can 
	 */
	@Override
	public Path toPath(String baseName, Object... args) {
		return basePath.resolve(baseName + ".properties");
	}

	@Override
	public PropertiesResource create(String baseName, Object... args) throws IOException {
		return new PropertiesResource(baseUri, basePath, baseName);
	}

}
