package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

public class CssResource extends AbstractFileResource {

	CssResource(final String baseName, final Path path) throws IOException {
		super(baseName, path);
	}

	@Override
	public String uri() {
		return null;
	}

	@Override
	public String mime() {
		return "text/css; charset=UTF-8";
	}

}
