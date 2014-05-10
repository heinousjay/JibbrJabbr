package jj.document;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.Location;
import jj.configuration.resolution.PathResolver;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class HtmlResourceCreator extends AbstractResourceCreator<HtmlResource> {
	
	public static String resourceName(String baseName) {
		return baseName + ".html";
	}
	
	private final PathResolver app;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	HtmlResourceCreator(
		final PathResolver app,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.app = app;
		this.instanceModuleCreator = instanceModuleCreator;
	}
	
	@Override
	protected URI uri(final Location base, final String name, final Object... args) {
		return app.resolvePath(base, name).toUri();
	}
	
	@Override
	public HtmlResource create(final Location base, final String name, final Object...args) throws IOException {
		return instanceModuleCreator.createResource(HtmlResource.class, resourceKey(base, name), base, name);
	}
}
