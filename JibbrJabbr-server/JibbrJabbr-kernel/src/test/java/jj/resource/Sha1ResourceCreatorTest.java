package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public class Sha1ResourceCreatorTest extends ResourceBase<Sha1Resource, Sha1ResourceCreator> {
	
	String sha1 = "12345678901234567890abcdefedca1234567890";
	FileTime fileTime = FileTime.fromMillis(1375646160223L);

	protected void resourceAssertions() throws Exception {
		assertThat(resource.representedSha(), is(sha1));
		assertThat(resource.representedFileTime(), is(fileTime));
	}

	@Override
	protected String baseName() {
		return "not.real.test.sha1";
	}

	@Override
	protected Path path() {
		return basePath.resolve(baseName());
	}

	@Override
	protected Sha1Resource resource() throws Exception {
		return new Sha1Resource(cacheKey(), baseName(), path());
	}

	@Override
	protected Sha1ResourceCreator toTest() {
		return new Sha1ResourceCreator(configuration, instanceModuleCreator);
	}

}
