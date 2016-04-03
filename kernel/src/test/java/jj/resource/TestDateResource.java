package jj.resource;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * @author jason
 */
class TestDateResource extends AbstractResource<Date> implements FileSystemResource {

	@Inject
	TestDateResource(ResourceIdentifier<?, ?> identifier) {
		super(new MockAbstractResourceDependencies(identifier));
	}

	@Override
	public boolean needsReplacing() throws IOException {
		return false;
	}

	@Override
	public String sha1() {
		return "";
	}

	@Override
	public Path path() {
		return Paths.get(name());
	}

	@Override
	public boolean isDirectory() {
		return false;
	}
}
