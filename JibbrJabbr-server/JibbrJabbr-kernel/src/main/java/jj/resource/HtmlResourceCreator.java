package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;

@Singleton
class HtmlResourceCreator implements ResourceCreator<HtmlResource>{
	
	private final Path basePath;
	
	@Inject
	HtmlResourceCreator(final Configuration configuration) {
		this.basePath = configuration.basePath();
	}

	@Override
	public Class<HtmlResource> type() {
		return HtmlResource.class;
	}
	
	@Override
	public boolean canLoad(String name, Object... args) {
		return true;
	}
	
	@Override
	public Path toPath(final String name, Object...args) {
		return basePath.resolve(name + ".html");
	}
	
	@Override
	public HtmlResource create(final String name, final Object...args) throws IOException {
		return new HtmlResource(name, toPath(name));
	}
}
