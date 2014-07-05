package jj.resource.sha1;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import jj.configuration.resolution.AppLocation;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.ResourceBase;
import jj.resource.sha1.Sha1Resource;
import jj.resource.sha1.Sha1ResourceCreator;

public class Sha1ResourceCreatorTest extends ResourceBase<Sha1Resource, Sha1ResourceCreator> {
	
	String sha1 = "12345678901234567890abcdefedca1234567890";
	FileTime fileTime = FileTime.fromMillis(1375646160223L);

	protected void resourceAssertions() throws Exception {
		assertThat(resource.representedSha(), is(sha1));
		assertThat(resource.representedFileTime(), is(fileTime));
	}

	@Override
	protected String name() {
		return "not.real.test.sha1";
	}

	protected Path path() {
		return appPath.resolve(name());
	}

	@Override
	protected Sha1Resource resource() throws Exception {
		return new Sha1Resource(new MockAbstractResourceDependencies(cacheKey(), AppLocation.Base), name(), path());
	}

	@Override
	protected Sha1ResourceCreator toTest() {
		return new Sha1ResourceCreator(pathResolver, creator);
	}

}
