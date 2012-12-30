package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.Configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HtmlResourceCreatorTest {
	
	// given
	URI baseUri;
	Path basePath;
	@Mock Configuration configuration;
	String baseName;
	
	@Before
	public void before() throws Exception {
		baseUri = URI.create("http://localhost:8080/");
		basePath = Paths.get(HtmlResourceCreatorTest.class.getResource("/index.html").toURI()).getParent();
		when(configuration.basePath()).thenReturn(basePath);
		when(configuration.baseUri()).thenReturn(baseUri);
		baseName = "internal/no-worky";
	}

	@Test
	public void testCreate() throws Exception {
		
		// given
		HtmlResourceCreator toTest = new HtmlResourceCreator(configuration);
		
		// when
		HtmlResource result = toTest.create(baseName);
		
		// then
		assertThat(result, is(not(nullValue())));
		assertThat(result.baseName(), is(baseName));
	}
	
	@Test
	public void testFileNotFound() throws Exception {
		// given
		baseName = "not/a/real/baseName";
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
