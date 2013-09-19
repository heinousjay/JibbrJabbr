package jj.resource.document;

import java.nio.file.Path;

import jj.resource.ResourceBase;
import jj.resource.document.HtmlResource;
import jj.resource.document.HtmlResourceCreator;

public class HtmlResourceCreatorTest extends ResourceBase<HtmlResource, HtmlResourceCreator> {

	@Override
	protected String baseName() {
		return "internal/no-worky";
	}
	
	protected Path path() {
		return appPath.resolve(baseName() + ".html");
	}
	
	@Override
	protected HtmlResource resource() throws Exception {
		return new HtmlResource(configuration, logger, cacheKey(), baseName(), path());
	}
	
	@Override
	protected HtmlResourceCreator toTest() {
		return new HtmlResourceCreator(configuration, creator);
	}
}