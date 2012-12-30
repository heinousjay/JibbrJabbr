package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

class PropertiesResourceCreator implements ResourceCreator<PropertiesResource> {

	private final URI baseUri;
	private final Path basePath;
	
	PropertiesResourceCreator(final URI baseUri, final Path basePath) {
		this.baseUri = baseUri;
		this.basePath = basePath;
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
