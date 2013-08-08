package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static java.nio.file.StandardOpenOption.*;


import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import jj.execution.JJExecutors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

/**
 * this test is a little concrete
 * tests
 * @author jason
 *
 */
public class ResourceFinderImplTest extends RealResourceBase {

	ResourceCache resourceCache;
	ResourceCreators resourceCreators;
	@Mock ResourceWatchService resourceWatchService;
	@Mock JJExecutors executors;
	@Mock Logger logger;
	
	@Mock AssetResourceCreator assetResourceCreator;
	@Mock HtmlResourceCreator htmlResourceCreator;
	@Mock ScriptResourceCreator scriptResourceCreator;
	@Mock StaticResourceCreator staticResourceCreator;
	
	ResourceFinder rfi;
	
	static class PathAnswer implements Answer<Path> {
		
		private final Path appPath;
		private final String bonus;
		
		PathAnswer(final Path appPath) {
			this.appPath = appPath;
			bonus = null;
		}
		
		PathAnswer(final Path appPath, final String bonus) {
			this.appPath = appPath;
			this.bonus = bonus;
		}

		@Override
		public Path answer(InvocationOnMock invocation) throws Throwable {
			return appPath.resolve(String.valueOf(invocation.getArguments()[0]) + (bonus == null ? "" : bonus));
		}
	};
	
	@Before
	public void before() throws Exception {
		
		given(assetResourceCreator.type()).willReturn(AssetResource.class);
		given(htmlResourceCreator.type()).willReturn(HtmlResource.class);
		given(scriptResourceCreator.type()).willReturn(ScriptResource.class);
		given(staticResourceCreator.type()).willReturn(StaticResource.class);
		
		given(assetResourceCreator.path(anyString(), anyVararg())).willAnswer(new PathAnswer(AssetResourceCreator.appPath));
		given(htmlResourceCreator.path(anyString(), anyVararg())).willAnswer(new PathAnswer(appPath, ".html"));
		given(scriptResourceCreator.path(anyString(), anyVararg())).willAnswer(new PathAnswer(appPath));
		given(staticResourceCreator.path(anyString(), anyVararg())).willAnswer(new PathAnswer(appPath));
		
		Map<Class<? extends Resource>, ResourceCreator<? extends Resource>> resourceCreatorsMap = new HashMap<>();
		resourceCreatorsMap.put(AssetResource.class, assetResourceCreator);
		resourceCreatorsMap.put(HtmlResource.class, htmlResourceCreator);
		resourceCreatorsMap.put(ScriptResource.class, scriptResourceCreator);
		resourceCreatorsMap.put(StaticResource.class, staticResourceCreator);
		
		resourceCreators = new ResourceCreators(resourceCreatorsMap);
		
		resourceCache = new ResourceCacheImpl(resourceCreators);
		
		given(executors.isIOThread()).willReturn(true);
		
		rfi = new ResourceFinderImpl(resourceCache, resourceCreators, resourceWatchService, executors);
	}
	
	@Test
	public void test() throws IOException {
		
		// given
		String baseName = "index";
		ScriptResource mockScriptResource = mock(ScriptResource.class);
		given(scriptResourceCreator.create(eq(ScriptResourceType.Server.suffix(baseName)), anyVararg())).willReturn(mockScriptResource);
		
		// when
		HtmlResource result1 = rfi.findResource(HtmlResource.class, "internal/no-worky");
		
		given(executors.isIOThread()).willReturn(true);
		ScriptResource result2 = rfi.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		given(executors.isIOThread()).willReturn(false);
		ScriptResource result3 = rfi.findResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		// loading again with no changes should result in no changes
		given(executors.isIOThread()).willReturn(true);
		ScriptResource result4 = rfi.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
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
		
		// given
		StaticResource mockStaticResource = mock(StaticResource.class);
		given(staticResourceCreator.create(eq("index.html"), anyVararg())).willReturn(mockStaticResource);
		HtmlResource mockHtmlResource = mock(HtmlResource.class);
		given(htmlResourceCreator.create(eq("index"), anyVararg())).willReturn(mockHtmlResource);
		
		// when
		StaticResource sr = rfi.loadResource(StaticResource.class, "index.html");
		HtmlResource hr = rfi.loadResource(HtmlResource.class, "index");
		
		// then 
		assertThat(sr, is(notNullValue()));
		assertThat(hr, is(notNullValue()));
	}
	
	@Test
	public void testStaticResources() throws IOException {
		
		// given
		StaticResource mockResource = mock(StaticResource.class);
		given(staticResourceCreator.create(eq("index.html"), anyVararg())).willReturn(mockResource);
		
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
	
	//@Test
	public void loadBigFile() throws IOException {
		String fileName = "big.fella";
		Path bigFella = configuration.appPath().resolve(fileName);
		try (SeekableByteChannel channel = Files.newByteChannel(bigFella, WRITE, CREATE_NEW, SPARSE)) {
			
			
			
		} finally {
			Files.deleteIfExists(bigFella);
			
		}
	}

}
