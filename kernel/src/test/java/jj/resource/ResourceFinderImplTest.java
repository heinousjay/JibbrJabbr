package jj.resource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;
import jj.application.AppLocation;
import jj.event.Publisher;
import jj.execution.CurrentTask;
import jj.http.server.resource.StaticResource;

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

	private @Mock
	ResourceCache resourceCache;
	private @Mock Publisher publisher;
	private @Mock CurrentTask currentTask;
	
	private @InjectMocks ResourceFinderImpl rfi;
	
	private @Mock SimpleResourceCreator<Void, StaticResource> staticResourceCreator;
	private @Mock SimpleResourceCreator<Sha1ResourceTarget, Sha1Resource> sha1ResourceCreator;
	
	private @Captor ArgumentCaptor<ResourceEvent> eventCaptor;
	
	private @Mock StaticResource staticResource1;
	private @Mock ResourceKey staticResource1Key;
	private @Mock StaticResource staticResource2;
	private @Mock ResourceKey staticResource2Key;
	
	private @Mock Sha1Resource sha1Resource1;
	private @Mock ResourceKey sha1Resource1Key;
	private @Mock Sha1Resource sha1Resource2;
	private @Mock ResourceKey sha1Resource2Key;
	
	private @Mock ResourceKey deadKey;
	
	private @Mock ResourceTask task;
	
	@Test
	public void testFindResource() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		
		given(staticResourceCreator.resourceKey(AppBase, name1, null)).willReturn(staticResource1Key);
		given(resourceCache.get(staticResource1Key)).willReturn(staticResource1);
		given(sha1ResourceCreator.resourceKey(AppBase, name2, null)).willReturn(sha1Resource2Key);
		given(resourceCache.get(sha1Resource2Key)).willReturn(sha1Resource2);
		
		assertThat(rfi.findResource(StaticResource.class, AppBase, name1), is(staticResource1));
		assertThat(rfi.findResource(StaticResource.class, AppBase, name2), is(nullValue()));
		assertThat(rfi.findResource(Sha1Resource.class, AppBase, name2, null), is(sha1Resource2));
		assertThat(rfi.findResource(Sha1Resource.class, AppBase, name1, null), is(nullValue()));
	}
	
	@Test
	public void testFindResourceWithBundle() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		
		given(staticResourceCreator.resourceKey(any(AppLocation.class), anyString(), anyObject())).willReturn(deadKey);
		given(staticResourceCreator.resourceKey(Public, name1, null)).willReturn(staticResource1Key);
		given(staticResourceCreator.resourceKey(Assets, name2, null)).willReturn(staticResource2Key);
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
		
		verify(staticResourceCreator).resourceKey(Public, name2, null);
		verify(staticResourceCreator).resourceKey(Private, name2, null);
		verify(staticResourceCreator).resourceKey(Assets, name2, null);
	}
	
	@Test
	public void testLoadResourceHappyPath() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(staticResourceCreator.type()).willReturn(StaticResource.class);
		given(staticResourceCreator.resourceKey(AppBase, name1, null)).willReturn(staticResource2Key);
		given(staticResourceCreator.create(AppBase, name1, null)).willReturn(staticResource2);
		
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		given(sha1ResourceCreator.type()).willReturn(Sha1Resource.class);
		given(sha1ResourceCreator.resourceKey(AppBase, name2, null)).willReturn(sha1Resource1Key);
		given(sha1ResourceCreator.create(AppBase, name2, null)).willReturn(sha1Resource1);
		
		given(currentTask.currentIs(ResourceTask.class)).willReturn(true);
		given(currentTask.currentAs(ResourceTask.class)).willReturn(task);
		
		assertThat(rfi.findResource(StaticResource.class, AppBase, name1), is(nullValue()));
		assertThat(rfi.findResource(Sha1Resource.class, AppBase, name2, null), is(nullValue()));
		
		rfi.loadResource(StaticResource.class, AppBase, name1);
		rfi.loadResource(Sha1Resource.class, AppBase, name2, null);
		
		verify(resourceCache).putIfAbsent(staticResource2Key, staticResource2);
		verify(resourceCache).putIfAbsent(sha1Resource1Key, sha1Resource1);
		
		verify(publisher, times(2)).publish(eventCaptor.capture());
		// validate the events? probably
	}
}
