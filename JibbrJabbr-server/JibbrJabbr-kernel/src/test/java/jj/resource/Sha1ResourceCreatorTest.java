package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.FileTime;

import org.junit.Test;

public class Sha1ResourceCreatorTest extends ResourceBase {
	
	String sha1 = "12345678901234567890abcdefedca1234567890";
	FileTime fileTime = FileTime.fromMillis(1375646160223L);

	@Test
	public void test() throws Exception {
		Sha1Resource r = testFileResource("not.real.test.sha1", new Sha1ResourceCreator(configuration));
		assertThat(r.representedSha(), is(sha1));
		assertThat(r.representedFileTime(), is(fileTime));
	}
	
	@Test
	public void testNotFound() throws Exception {
		try {
			new Sha1ResourceCreator(configuration).create("index.html");
			fail();
		} catch (NoSuchFileException nsfe) {
			assertThat(nsfe.getMessage(), is(configuration.basePath().resolve("index.html").toString()));
		}
	}

}
