package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jj.JJExecutors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * this test is fairly concrete and not so much "unit" but
 * as it deals with the file system it needs to be, by
 * necessity.  i don't consider this a problem.
 * @author jason
 *
 */
public class ConcreteResourceFinderImplTest extends ResourceBase {

	ResourceCache resourceCache;
	Set<ResourceCreator<?>> resourceCreators;
	@Mock ResourceWatchServiceImpl resourceWatchService;
	
	@Mock JJExecutors executors;
	
	@Before
	public void before() throws Exception {
		
		resourceCache = new ResourceCache();
		
		resourceCreators = new HashSet<>();
		resourceCreators.add(new HtmlResourceCreator(configuration));
		resourceCreators.add(new ScriptResourceCreator(configuration));
		resourceCreators.add(new StaticResourceCreator(configuration));
		
		
	}
	
	@Test
	public void test() throws IOException {
		
		// given 
		ResourceFinderImpl toTest = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService, executors);
		
		// when
		HtmlResource result1 = toTest.findResource(HtmlResource.class, "internal/no-worky");
		
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
		
		verify(resourceWatchService, never()).watch(result1);
		// watch service should only have been asked to watch this once, the first time it was created
		verify(resourceWatchService).watch(result2);

		// given
		given(executors.isIOThread()).willReturn(true);
	}
	
	@Test
	public void staticResources() throws IOException {
		
		//given
		ResourceFinderImpl toTest = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService, executors);
	}

}
