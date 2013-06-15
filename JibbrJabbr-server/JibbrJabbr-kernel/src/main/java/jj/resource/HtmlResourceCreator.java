package jj.resource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;

@Singleton
class HtmlResourceCreator implements ResourceCreator<HtmlResource>{
	
	private final URI baseUri;
	
	private final Path basePath;
	
	@Inject
	HtmlResourceCreator(final Configuration configuration) {
		this.baseUri = configuration.baseUri();
		this.basePath = configuration.basePath();
	}

	@Override
	public Class<HtmlResource> type() {
		return HtmlResource.class;
	}
	
	@Override
	public Path toPath(final String baseName, Object...args) {
		return basePath.resolve(baseName + ".html");
	}
	
	@Override
	public HtmlResource create(final String baseName, final Object...args) throws IOException {
		return new HtmlResource(baseUri.resolve(baseName), toPath(baseName));
	}
}
