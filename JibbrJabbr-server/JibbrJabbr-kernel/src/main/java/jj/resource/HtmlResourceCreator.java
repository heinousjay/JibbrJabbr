package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Configuration;

@Singleton
class HtmlResourceCreator extends AbstractResourceCreator<HtmlResource>{
	
	private final Configuration configuration;
	private final ResourceInstanceModuleCreator instanceModuleCreator;
	
	@Inject
	HtmlResourceCreator(
		final Configuration configuration,
		final ResourceInstanceModuleCreator instanceModuleCreator
	) {
		this.configuration = configuration;
		this.instanceModuleCreator = instanceModuleCreator;
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
	Path path(final String baseName, Object...args) {
		return configuration.basePath().resolve(baseName + ".html");
	}
	
	@Override
	public HtmlResource create(final String baseName, final Object...args) throws IOException {
		return instanceModuleCreator.createResource(HtmlResource.class, cacheKey(baseName), baseName, path(baseName));
	}
}
