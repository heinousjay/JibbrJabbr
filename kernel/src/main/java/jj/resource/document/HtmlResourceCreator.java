package jj.resource.document;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Arguments;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class HtmlResourceCreator extends AbstractResourceCreator<HtmlResource> {
	
	public static String resourceName(String baseName) {
		return baseName + ".html";
	}
	
	private final Arguments arguments;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	HtmlResourceCreator(
		final Arguments arguments,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.arguments = arguments;
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
	protected URI uri(String baseName, Object... args) {
		return path(baseName).toUri();
	}
	
	private Path path(final String baseName, Object...args) {
		return arguments.appPath().resolve(baseName);
	}
	
	@Override
	public HtmlResource create(final String baseName, final Object...args) throws IOException {
		return instanceModuleCreator.createResource(HtmlResource.class, cacheKey(baseName), baseName, path(baseName));
	}
}
