/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.resource;

import static org.mockito.BDDMockito.*;
import static jj.configuration.resolution.AppLocation.Base;
import jj.event.MockPublisher;
import jj.event.Publisher;
import jj.resource.AbstractResource.Dependencies;
import jj.util.MockClock;

/**
 * to simplify resource testing
 * 
 * @author jason
 *
 */
public class MockAbstractResourceDependencies extends Dependencies {
	
	public final DirectoryResource rootDirectory = mock(DirectoryResource.class);
	
	public static class MockInnerAbstractResourceDependencies extends AbstractResource.AbstractResourceDependencies {

		public MockInnerAbstractResourceDependencies() {
			this(mock(ResourceFinder.class));
		}
		
		public MockInnerAbstractResourceDependencies(
			ResourceFinder resourceFinder
		) {
			super(
				new MockClock(),
				mock(ResourceConfiguration.class),
				mock(AbstractResourceEventDemuxer.class),
				new MockPublisher(),
				resourceFinder
			);
		}

		
		public MockInnerAbstractResourceDependencies(
			Publisher publisher
		) {
			super(
				new MockClock(),
				mock(ResourceConfiguration.class),
				mock(AbstractResourceEventDemuxer.class),
				publisher,
				mock(ResourceFinder.class)
			);
		}
		
		public MockPublisher publisher() {
			return (MockPublisher)publisher;
		}

		public ResourceFinder resourceFinder() {
			return resourceFinder;
		}
	}

	public MockAbstractResourceDependencies(Location base, String name) {
		this(
			base,
			name,
			mock(ResourceFinder.class)
		);
	}

	public MockAbstractResourceDependencies(Location base, String name, ResourceFinder resourceFinder) {
		super(
			new MockInnerAbstractResourceDependencies(resourceFinder),
			mock(ResourceKey.class),
			base,
			name
		);
	}
	
	public MockAbstractResourceDependencies(ResourceKey resourceKey, Location base, String name) {
		super(
			new MockInnerAbstractResourceDependencies(),
			resourceKey,
			base,
			name
		);
	}
	
	public MockAbstractResourceDependencies(ResourceKey resourceKey, Location base, String name, Publisher publisher) {
		super(
			new MockInnerAbstractResourceDependencies(publisher),
			resourceKey,
			base,
			name
		);
	}
	
	{
		given(abstractResourceDependencies.resourceFinder.findResource(DirectoryResource.class, Base, "")).willReturn(rootDirectory);
		given(abstractResourceDependencies.resourceConfiguration.maxFileSizeToLoad()).willReturn(1024 * 1024 * 10L);
		FileTypeSettingsDefaultProvider provider = new FileTypeSettingsDefaultProvider();
		given(abstractResourceDependencies.resourceConfiguration.fileTypeSettings()).willReturn(provider.get());
		DefaultSettingsDefaultProvider provider2 = new DefaultSettingsDefaultProvider();
		given(abstractResourceDependencies.resourceConfiguration.defaultSettings()).willReturn(provider2.get());
	}
	
	public MockClock clock() {
		return (MockClock)abstractResourceDependencies.clock;
	}
	
	public ResourceConfiguration resourceConfiguration() {
		return abstractResourceDependencies.resourceConfiguration;
	}
	
	public AbstractResourceEventDemuxer abstractResourceInitializationListener() {
		return abstractResourceDependencies.demuxer;
	}

	public ResourceFinder resourceFinder() {
		return abstractResourceDependencies.resourceFinder;
	}
	
	public MockPublisher publisher() {
		return (MockPublisher)abstractResourceDependencies.publisher;
	}
	
	public ResourceKey resourceKey() {
		return resourceKey;
	}
	
	public Location base() {
		return base;
	}
	
	public String name() {
		return name;
	}
}
