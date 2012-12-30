package jj.resource;

import java.io.IOException;
import java.nio.file.Path;

public class CssResource extends AbstractResource {

	CssResource(final String baseName, final Path path) throws IOException {
		super(baseName, path);
	}

	@Override
	public String uri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String absoluteUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String mime() {
		return "text/css; charset=UTF-8";
	}

}
