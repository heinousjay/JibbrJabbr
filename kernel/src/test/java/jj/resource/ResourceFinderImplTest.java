package jj.resource;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.application.AppLocation.*;
import static jj.server.ServerLocation.*;
import jj.event.Publisher;
import jj.execution.CurrentTask;
import jj.http.server.resource.StaticResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

@RunWith(MockitoJUnitRunner.class)
public class ResourceFinderImplTest {
	
	private final String name1 = "index.html";
	private final String name2 = "output.html";

	private @Mock ResourceCache resourceCache;
	private @Mock Publisher publisher;
	private @Mock CurrentTask currentTask;
	private ResourceIdentifierMaker resourceIdentifierMaker;
	
	private ResourceFinderImpl rfi;
	
	private @Mock SimpleResourceCreator<Void, StaticResource> staticResourceCreator;
	private @Mock SimpleResourceCreator<Sha1ResourceTarget, Sha1Resource> sha1ResourceCreator;
	
	private @Captor ArgumentCaptor<ResourceEvent> eventCaptor;
	
	private @Mock StaticResource staticResource1;
	private ResourceIdentifier<StaticResource, Void> sr1Key = ResourceIdentifierHelper.make(StaticResource.class, Public, name1);
	private @Mock StaticResource staticResource2;
	private ResourceIdentifier<StaticResource, Void> sr2Key = ResourceIdentifierHelper.make(StaticResource.class, Private, name2);

	private @Mock Sha1Resource sha1Resource1;
	private Sha1ResourceTarget sha1Target1;
	private ResourceIdentifier<Sha1Resource, Sha1ResourceTarget> sh1Key;

	private @Mock Sha1Resource sha1Resource2;
	private Sha1ResourceTarget sha1Target2;
	private ResourceIdentifier<Sha1Resource, Sha1ResourceTarget> sh2Key;

	private @Mock ResourceTask task;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {

		resourceIdentifierMaker = new MockResourceIdentifierMaker();

		rfi = new ResourceFinderImpl(resourceCache, publisher, currentTask, resourceIdentifierMaker);

		sha1Target1 = new Sha1ResourceTarget(staticResource1);
		sh1Key = ResourceIdentifierHelper.make(Sha1Resource.class, AppBase, name1, sha1Target1);

		sha1Target2 = new Sha1ResourceTarget(staticResource2);
		sh2Key = ResourceIdentifierHelper.make(Sha1Resource.class, AppBase, name2, sha1Target2);

		given((ResourceIdentifier<StaticResource, Void>)staticResource1.identifier()).willReturn(sr1Key);
		given((ResourceIdentifier<StaticResource, Void>)staticResource2.identifier()).willReturn(sr2Key);
		given((ResourceIdentifier<Sha1Resource, Sha1ResourceTarget>)sha1Resource1.identifier()).willReturn(sh1Key);
		given((ResourceIdentifier<Sha1Resource, Sha1ResourceTarget>)sha1Resource2.identifier()).willReturn(sh2Key);
	}
	
	@Test
	public void testFindResource() throws Exception {

		given(resourceCache.get(sr1Key)).willReturn(staticResource1);
		given(resourceCache.get(sh1Key)).willReturn(sha1Resource1);

		assertThat(rfi.findResource(StaticResource.class, Public, name1), is(staticResource1));
		assertThat(rfi.findResource(StaticResource.class, Public, name2), is(nullValue()));
		assertThat(rfi.findResource(Sha1Resource.class, AppBase, name1, sha1Target1), is(sha1Resource1));
		assertThat(rfi.findResource(Sha1Resource.class, AppBase, name1, sha1Target2), is(nullValue()));

		assertThat(rfi.findResource(sr1Key), is(staticResource1));
		assertThat(rfi.findResource(sr2Key), is(nullValue()));
		assertThat(rfi.findResource(sh1Key), is(sha1Resource1));
		assertThat(rfi.findResource(sh2Key), is(nullValue()));
	}
	
	@Test
	public void testFindResourceWithBundle() throws Exception {
		
		given(resourceCache.get(sr1Key)).willReturn(staticResource1);
		given(resourceCache.get(sr2Key)).willReturn(staticResource2);

		StaticResource sr = rfi.findResource(
			StaticResource.class,
			Public.and(Private).and(Assets),
			name1
		);

		assertThat(sr, is(staticResource1));
		
		sr = rfi.findResource(
			StaticResource.class,
			Public.and(Private).and(Assets),
			name2
		);
		
		assertThat(sr, is(staticResource2));
	}
	
	@Test
	public void testLoadResourceHappyPath() throws Exception {
		
		given(resourceCache.getCreator(StaticResource.class)).willReturn(staticResourceCreator);
		given(staticResourceCreator.type()).willReturn(StaticResource.class);
		given(staticResourceCreator.create(AppBase, name1, null)).willReturn(staticResource2);
		
		given(resourceCache.getCreator(Sha1Resource.class)).willReturn(sha1ResourceCreator);
		given(sha1ResourceCreator.type()).willReturn(Sha1Resource.class);
		given(sha1ResourceCreator.create(AppBase, name2, null)).willReturn(sha1Resource1);
		
		given(currentTask.currentIs(ResourceTask.class)).willReturn(true);
		given(currentTask.currentAs(ResourceTask.class)).willReturn(task);

		rfi.loadResource(StaticResource.class, AppBase, name1);
		rfi.loadResource(Sha1Resource.class, AppBase, name2, null);

		verify(resourceCache).putIfAbsent(staticResource2);
		verify(resourceCache).putIfAbsent(sha1Resource1);

		verify(publisher, times(2)).publish(eventCaptor.capture());

		for (ResourceEvent event : eventCaptor.getAllValues()) {
			assertThat(event, is(instanceOf(ResourceLoaded.class)));
			// TODO fix these details
		}
	}
}
