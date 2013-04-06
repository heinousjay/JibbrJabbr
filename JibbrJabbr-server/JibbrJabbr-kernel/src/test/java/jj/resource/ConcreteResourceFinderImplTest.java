package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;


import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import jj.Configuration;
import jj.JJExecutors;

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
	@Mock Configuration configuration;
	String baseName;
	ResourceCache resourceCache;
	Set<ResourceCreator<?>> resourceCreators;
	@Mock ResourceWatchServiceImpl resourceWatchService;
	
	@Mock JJExecutors executors;
	
	@Before
	public void before() throws Exception {
		baseUri = URI.create("http://localhost:8080/");
		basePath = Paths.get(ConcreteResourceFinderImplTest.class.getResource("/index.html").toURI()).getParent();
		when(configuration.basePath()).thenReturn(basePath);
		when(configuration.baseUri()).thenReturn(baseUri);
		
		baseName = "internal/no-worky";
		resourceCache = new ResourceCache();
		resourceCreators = new HashSet<>();
		resourceCreators.add(new HtmlResourceCreator(configuration));
		resourceCreators.add(new ScriptResourceCreator(configuration));
	}
	
	@Test
	public void test() throws IOException {
		
		// given 
		ResourceFinderImpl toTest = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService, executors);
		
		// when
		HtmlResource result1 = toTest.findResource(HtmlResource.class, baseName);
		
		given(executors.isIOThread()).willReturn(true);
		ScriptResource result2 = toTest.loadResource(ScriptResource.class, "index", ScriptResourceType.Server);
		
		given(executors.isIOThread()).willReturn(false);
		ScriptResource result3 = toTest.findResource(ScriptResource.class, "index", ScriptResourceType.Server);
		
		// loading again with no changes should result in no changes
		given(executors.isIOThread()).willReturn(true);
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
