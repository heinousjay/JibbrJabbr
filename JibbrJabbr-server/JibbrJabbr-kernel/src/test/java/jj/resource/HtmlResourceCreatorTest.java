package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import org.junit.Test;

public class HtmlResourceCreatorTest extends ResourceBase {


	@Test
	public void test() throws Exception {
		
		doTest("internal/no-worky");
	}
	
	private void doTest(final String baseName) throws Exception {

		HtmlResource resource1 = testFileResource(baseName, new HtmlResourceCreator(configuration));
		assertThat(resource1, is(notNullValue()));
		assertThat(resource1.mime(), is(MimeTypes.get(".html")));
		assertThat(resource1.byteBuffer.readableBytes(), is(Files.readAllBytes(resource1.path()).length));
		assertThat(resource1.document(), is(notNullValue()));
	}
	
	@Test
	public void testFileNotFound() throws Exception {
		// given
		String baseName = "not/a/real/baseName";
		HtmlResourceCreator toTest = new HtmlResourceCreator(configuration);
		
		// when
		try {
			toTest.create(baseName);
			
		// then
			fail("should have thrown");
		} catch (NoSuchFileException nsfe) {
			
		}
	}

}
