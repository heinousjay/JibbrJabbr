package jj.document;

import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.AppLocation;
import jj.configuration.Application;
import jj.resource.AbstractResourceCreator;
import jj.resource.ResourceInstanceCreator;

@Singleton
public class HtmlResourceCreator extends AbstractResourceCreator<HtmlResource> {
	
	public static String resourceName(String baseName) {
		return baseName + ".html";
	}
	
	private final Application app;
	private final ResourceInstanceCreator instanceModuleCreator;
	
	@Inject
	HtmlResourceCreator(
		final Application app,
		final ResourceInstanceCreator instanceModuleCreator
	) {
		this.app = app;
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
	protected URI uri(final AppLocation base, final String name, final Object... args) {
		return app.resolvePath(base, name).toUri();
	}
	
	@Override
	public HtmlResource create(final AppLocation base, final String name, final Object...args) throws IOException {
		return instanceModuleCreator.createResource(HtmlResource.class, cacheKey(base, name), base, name);
	}
}
