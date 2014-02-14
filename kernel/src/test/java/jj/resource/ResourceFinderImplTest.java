package jj.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jj.event.Publisher;
import jj.execution.IsThread;
import jj.resource.asset.Asset;
import jj.resource.asset.AssetResource;
import jj.resource.document.HtmlResource;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceType;
import jj.resource.stat.ic.StaticResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

/**
 * verifies the POWER
 * @author jason
 *
 */
public class ResourceFinderImplTest extends RealResourceBase {

	ResourceCache resourceCache;
	ResourceCreators resourceCreators;
	
	@Mock ResourceWatchService resourceWatchService;
	@Mock Publisher publisher;
	@Mock IsThread isThread;
	@Mock Logger logger;
	
	@Mock AbstractResourceCreator<AssetResource> assetResourceCreator;
	@Mock AbstractResourceCreator<HtmlResource> htmlResourceCreator;
	@Mock AbstractResourceCreator<ScriptResource> scriptResourceCreator;
	@Mock AbstractResourceCreator<StaticResource> staticResourceCreator;
	
	@Captor ArgumentCaptor<ResourceEvent> eventCaptor;
	
	ResourceFinder rfi;
	
	static class URIAnswer implements Answer<URI> {
		
		private final Path appPath;
		private final String bonus;
		
		URIAnswer(final Path appPath) {
			this.appPath = appPath;
			bonus = null;
		}
		
		URIAnswer(final Path appPath, final String bonus) {
			this.appPath = appPath;
			this.bonus = bonus;
		}

		@Override
		public URI answer(InvocationOnMock invocation) throws Throwable {
			return appPath.resolve(String.valueOf(invocation.getArguments()[0]) + (bonus == null ? "" : bonus)).toUri();
		}
	};
	
	@Before
	public void before() throws Exception {
		
		given(assetResourceCreator.type()).willReturn(AssetResource.class);
		given(htmlResourceCreator.type()).willReturn(HtmlResource.class);
		given(scriptResourceCreator.type()).willReturn(ScriptResource.class);
		given(staticResourceCreator.type()).willReturn(StaticResource.class);
		
		given(assetResourceCreator.uri(anyString(), anyVararg())).willAnswer(new URIAnswer(Asset.appPath));
		given(htmlResourceCreator.uri(anyString(), anyVararg())).willAnswer(new URIAnswer(appPath, ".html"));
		given(scriptResourceCreator.uri(anyString(), anyVararg())).willAnswer(new URIAnswer(appPath));
		given(staticResourceCreator.uri(anyString(), anyVararg())).willAnswer(new URIAnswer(appPath));
		
		Map<Class<? extends Resource>, ResourceCreator<? extends Resource>> resourceCreatorsMap = new HashMap<>();
		resourceCreatorsMap.put(AssetResource.class, assetResourceCreator);
		resourceCreatorsMap.put(HtmlResource.class, htmlResourceCreator);
		resourceCreatorsMap.put(ScriptResource.class, scriptResourceCreator);
		resourceCreatorsMap.put(StaticResource.class, staticResourceCreator);
		
		resourceCreators = new ResourceCreators(resourceCreatorsMap);
		
		// you can pass null, if you never call start it won't matter
		resourceCache = new ResourceCacheImpl(resourceCreators, null);
		
		given(isThread.forIO()).willReturn(true);
		
		rfi = new ResourceFinderImpl(resourceCache, resourceWatchService, publisher, isThread);
	}
	
	@Test
	public void test() throws IOException {
		
		// given
		String baseName = "index";
		ScriptResource mockScriptResource = mock(ScriptResource.class);
		given(scriptResourceCreator.create(eq(ScriptResourceType.Server.suffix(baseName)), anyVararg())).willReturn(mockScriptResource);
		
		// when
		HtmlResource result1 = rfi.findResource(HtmlResource.class, "internal/no-worky");
		
		given(isThread.forIO()).willReturn(true);
		ScriptResource result2 = rfi.loadResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		given(isThread.forIO()).willReturn(false);
		ScriptResource result3 = rfi.findResource(ScriptResource.class, ScriptResourceType.Server.suffix(baseName));
		
		// loading again with no changes should result in no changes
		given(isThread.forIO()).willReturn(true);
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
		
		// and make sure we published our event
		verify(publisher).publish(eventCaptor.capture());
		assertThat(eventCaptor.getValue(), is(instanceOf(ResourceLoadedEvent.class)));
		assertTrue(eventCaptor.getValue().matches(ScriptResource.class, ScriptResourceType.Server.suffix(baseName)));
	}
	
	@Test
	public void testObseleteResourceIsReloaded() throws Exception {
		
		ResourceMaker resourceMaker = new ResourceMaker(configuration, arguments);
		
		StaticResource staticResource1 = resourceMaker.makeStatic("index.html");
		StaticResource staticResource2 = resourceMaker.makeStatic("index.html");
		given(staticResourceCreator.create(eq("index.html"), anyVararg())).willReturn(staticResource1, staticResource2);
		
		StaticResource sr1 = rfi.loadResource(StaticResource.class, "index.html");
		StaticResource sr2 = rfi.loadResource(StaticResource.class, "index.html");
		((AbstractResource)staticResource1).kill();
		
		StaticResource sr3 = rfi.loadResource(StaticResource.class, "index.html");
		
		assertThat(sr1, is(staticResource1));
		assertThat(sr2, is(staticResource1));
		assertThat(sr3, is(staticResource2));
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
}
