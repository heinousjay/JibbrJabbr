package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * this test is fairly concrete and not so much "unit" but
 * as it deals with the file system it needs to be, by
 * necessity.  i don't consider this a problem.
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ConcreteResourceFinderImplTest {

	URI baseUri;
	Path basePath;
	String baseName;
	ResourceCache resourceCache;
	ResourceCreator<?>[] resourceCreators;
	@Mock ResourceWatchService resourceWatchService;
	
	@Before
	public void before() throws Exception {
		baseUri = URI.create("http://localhost:8080/");
		basePath = Paths.get(HtmlResourceCreatorTest.class.getResource("/index.html").toURI()).getParent();
		baseName = "internal/no-worky";
		resourceCache = new ResourceCache();
		resourceCreators = new ResourceCreator<?>[] {
			new HtmlResourceCreator(baseUri, basePath),
			new ScriptResourceCreator(baseUri, basePath)
		};
	}
	
	@Test
	public void test() throws IOException {
		
		// given 
		ResourceFinderImpl toTest = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService);
		
		// when
		HtmlResource result1 = toTest.findResource(HtmlResource.class, baseName);
		ScriptResource result2 = toTest.loadResource(ScriptResource.class, "index", ScriptResourceType.Server);
		ScriptResource result3 = toTest.findResource(ScriptResource.class, "index", ScriptResourceType.Server);
		// loading again with no changes should result in no changes
		ScriptResource result4 = toTest.loadResource(ScriptResource.class, "index", ScriptResourceType.Server);
		
		// then
		assertThat(result1, is(nullValue()));
		assertThat(result2, is(notNullValue()));
		assertThat(result3, is(notNullValue()));
		assertThat(result4, is(notNullValue()));
		assertThat(result2, is(result3));
		assertThat(result3, is(result4));
		// watch service should only have been asked to watch this once, the first time it was created
		verify(resourceWatchService).watch((Resource)anyObject());
	}

}
