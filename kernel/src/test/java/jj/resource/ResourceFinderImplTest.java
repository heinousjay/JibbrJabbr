package jj.resource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.configuration.AppLocation.*;
import jj.configuration.AppLocation;
import jj.event.Publisher;
import jj.execution.CurrentTask;
import jj.resource.sha1.Sha1Resource;
import jj.resource.stat.ic.StaticResource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceFinderImplTest {
	
	private final String name1 = "index.html";
	private final String name2 = "output.html";

	private @Mock ResourceCache resourceCache;
	private @Mock ResourceWatchService resourceWatchService;
	private @Mock Publisher publisher;
	private @Mock IsThread isThread;
	private @Mock CurrentTask currentTask;
	
	private @InjectMocks ResourceFinderImpl rfi;
	
	private @Mock AbstractResourceCreator<StaticResource> staticResourceCreator;
	private @Mock AbstractResourceCreator<Sha1Resource> sha1ResourceCreator;
	
	private @Captor ArgumentCaptor<ResourceEvent> eventCaptor;
	
	private @Mock StaticResource staticResource1;
	private @Mock ResourceCacheKey staticResource1Key;
	private @Mock StaticResource staticResource2;
	private @Mock ResourceCacheKey staticResource2Key;
	
	private @Mock Sha1Resource sha1Resource1;
	private @Mock ResourceCacheKey sha1Resource1Key;
	private @Mock Sha1Resource sha1Resource2;
	private @Mock ResourceCacheKey sha1Resource2Key;
	
	private @Mock ResourceCacheKey deadKey;
	
	private @Mock IOTask task;
	
	@Test
	public void testFindResource() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		
		given(staticResourceCreator.cacheKey(Base, name1)).willReturn(staticResource1Key);
		given(resourceCache.get(staticResource1Key)).willReturn(staticResource1);
		given(sha1ResourceCreator.cacheKey(Base, name2)).willReturn(sha1Resource2Key);
		given(resourceCache.get(sha1Resource2Key)).willReturn(sha1Resource2);
		
		assertThat(rfi.findResource(StaticResource.class, Base, name1), is(staticResource1));
		assertThat(rfi.findResource(StaticResource.class, Base, name2), is(nullValue()));
		assertThat(rfi.findResource(Sha1Resource.class, Base, name2), is(sha1Resource2));
		assertThat(rfi.findResource(Sha1Resource.class, Base, name1), is(nullValue()));
	}
	
	@Test
	public void testFindResourceWithBundle() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		
		given(staticResourceCreator.cacheKey(any(AppLocation.class), anyString())).willReturn(deadKey);
		given(staticResourceCreator.cacheKey(Public, name1)).willReturn(staticResource1Key);
		given(staticResourceCreator.cacheKey(Assets, name2)).willReturn(staticResource2Key);
		given(resourceCache.get(staticResource1Key)).willReturn(staticResource1);
		given(resourceCache.get(staticResource2Key)).willReturn(staticResource2);
		
		StaticResource sr = rfi.findResource(
			StaticResource.class,
			Public.and(Private).and(Assets),
			name1
		);
		
		assertThat(sr, is(staticResource1));
		verify(resourceCache, never()).get(deadKey); // verifying it stopped at public
		
		sr = rfi.findResource(
			StaticResource.class,
			Public.and(Private).and(Assets),
			name2
		);
		
		assertThat(sr, is(staticResource2));
		
		verify(staticResourceCreator).cacheKey(Public, name2);
		verify(staticResourceCreator).cacheKey(Private, name2);
		verify(staticResourceCreator).cacheKey(Assets, name2);
	}
	
	@Test
	public void testLoadResourceHappyPath() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(staticResourceCreator.type()).willReturn(StaticResource.class);
		given(staticResourceCreator.cacheKey(Base, name1)).willReturn(staticResource2Key);
		given(staticResourceCreator.create(Base, name1)).willReturn(staticResource2);
		
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		given(sha1ResourceCreator.type()).willReturn(Sha1Resource.class);
		given(sha1ResourceCreator.cacheKey(Base, name2)).willReturn(sha1Resource1Key);
		given(sha1ResourceCreator.create(Base, name2)).willReturn(sha1Resource1);
		
		given(isThread.forIO()).willReturn(true);
		given(currentTask.currentAs(IOTask.class)).willReturn(task);
		
		assertThat(rfi.findResource(StaticResource.class, Base, name1), is(nullValue()));
		assertThat(rfi.findResource(Sha1Resource.class, Base, name2), is(nullValue()));
		
		rfi.loadResource(StaticResource.class, Base, name1);
		rfi.loadResource(Sha1Resource.class, Base, name2);
		
		verify(resourceCache).putIfAbsent(staticResource2Key, staticResource2);
		verify(resourceWatchService).watch(staticResource2);
		verify(resourceCache).putIfAbsent(sha1Resource1Key, sha1Resource1);
		verify(resourceWatchService).watch(sha1Resource1);
		
		verify(publisher, times(2)).publish(eventCaptor.capture());
		// validate the events? probably
	}
}
