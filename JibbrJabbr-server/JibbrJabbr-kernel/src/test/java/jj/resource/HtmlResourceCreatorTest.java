package jj.resource;

import java.nio.file.Path;

public class HtmlResourceCreatorTest extends ResourceBase<HtmlResource, HtmlResourceCreator> {

	@Override
	protected String baseName() {
		return "internal/no-worky";
	}
	
	@Override
	protected Path path() {
		return appPath.resolve(baseName() + ".html");
	}
	
	@Override
	protected HtmlResource resource() throws Exception {
		return new HtmlResource(cacheKey(), baseName(), path());
	}
	
	@Override
	protected HtmlResourceCreator toTest() {
		return new HtmlResourceCreator(configuration, instanceModuleCreator);
	}
}
