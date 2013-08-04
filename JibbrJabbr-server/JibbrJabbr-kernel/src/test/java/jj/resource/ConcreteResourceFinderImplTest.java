package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jj.execution.DevNullExecutionTraceImpl;
import jj.execution.JJExecutors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;

/**
 * this test is fairly concrete on purpose
 * @author jason
 *
 */
public class ConcreteResourceFinderImplTest extends ResourceBase {

	ResourceCache resourceCache;
	Set<ResourceCreator<?>> resourceCreators;
	@Mock ResourceWatchServiceImpl resourceWatchService;
	
	@Mock JJExecutors executors;
	
	ResourceFinderImpl rfi;
	
	@Before
	public void before() throws Exception {
		
		resourceCreators = new HashSet<>();
		resourceCreators.add(new AssetResourceCreator());
		resourceCreators.add(new CssResourceCreator(
			configuration, 
			new LessProcessor(configuration, new DevNullExecutionTraceImpl()),
			mock(ResourceFinder.class), // doesn't matter for the purposes of this test
			mock(Logger.class) // doesn't matter for the purposes of this test
		));
		resourceCreators.add(new HtmlResourceCreator(configuration));
		resourceCreators.add(new PropertiesResourceCreator(configuration));
		resourceCreators.add(new ScriptResourceCreator(configuration));
		resourceCreators.add(new StaticResourceCreator(configuration));
		
		resourceCache = new ResourceCache(resourceCreators);
		
		rfi = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService, executors);
		
		given(executors.isIOThread()).willReturn(true);
	}
	
	@Test
	public void test() throws IOException {
		
		// when
		HtmlResource result1 = rfi.findResource(HtmlResource.class, "internal/no-worky");
		
		given(executors.isIOThread()).willReturn(true);
		ScriptResource result2 = rfi.loadResource(ScriptResource.class, "index", ScriptResourceType.Server);
		
		given(executors.isIOThread()).willReturn(false);
		ScriptResource result3 = rfi.findResource(ScriptResource.class, "index", ScriptResourceType.Server);
		
		// loading again with no changes should result in no changes
		given(executors.isIOThread()).willReturn(true);
		ScriptResource result4 = rfi.loadResource(ScriptResource.class, "index", ScriptResourceType.Server);
		
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
	public void testMultipleResourceForOneFile() throws IOException {
		// this test is to verify that we can load the same file as
		// multiple resource implementations
		
		
		StaticResource sr = rfi.loadResource(StaticResource.class, "index.html");
		HtmlResource hr = rfi.loadResource(HtmlResource.class, "index");
		
		assertThat(sr, is(notNullValue()));
		assertThat(hr, is(notNullValue()));
		
		// they should be the same thing
		assertThat(hr.sha1(), is(sr.sha1()));
	}
	
	@Test
	public void testStaticResources() throws IOException {
		
		// when
		StaticResource resource1 = rfi.findResource(StaticResource.class, "index.html");
		
		// then
		assertThat(resource1, is(nullValue()));
		
		// when
		StaticResource resource2 = rfi.loadResource(StaticResource.class, "index.html");
		
		// then
		assertThat(resource2, is(notNullValue()));
		
		// when
		StaticResource resource3 = rfi.loadResource(StaticResource.class, "index.html");
		
		// then
		assertThat(resource3, is(notNullValue()));
		assertThat(resource2, is(sameInstance(resource3)));
		assertThat(resource2.size(), is(resource3.size()));
		
	}

}
