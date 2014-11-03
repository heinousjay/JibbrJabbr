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
package jj.script;

import static org.mockito.Mockito.mock;

import javax.inject.Provider;

import jj.event.Publisher;
import jj.resource.AbstractResourceEventDemuxer;
import jj.resource.ResourceConfiguration;
import jj.resource.ResourceFinder;
import jj.resource.ResourceKey;
import jj.script.AbstractScriptEnvironment.Dependencies;
import jj.util.MockClock;

/**
 * @author jason
 *
 */
public class MockAbstractScriptEnvironmentDependencies extends Dependencies {
	
	public interface MockPendingKeyProvider extends Provider<ContinuationPendingKey> {}
	
	public MockAbstractScriptEnvironmentDependencies() {
		this("unnamed");
	}

	public MockAbstractScriptEnvironmentDependencies(final String name) {
		this(name, mock(ResourceFinder.class));
	}

	public MockAbstractScriptEnvironmentDependencies(final String name, final ResourceFinder resourceFinder) {
		super(
			new MockClock(),
			mock(ResourceConfiguration.class),
			mock(AbstractResourceEventDemuxer.class),
			mock(ResourceKey.class),
			name,
			new MockRhinoContextProvider(),
			mock(ContinuationPendingCache.class),
			mock(MockPendingKeyProvider.class),
			mock(RequireInnerFunction.class),
			mock(InjectFunction.class),
			mock(Timers.class),
			mock(Publisher.class),
			resourceFinder
		);
	}

	public MockAbstractScriptEnvironmentDependencies(final RealRhinoContextProvider rhinoContextProvider, final String name) {
		super(
			new MockClock(),
			mock(ResourceConfiguration.class),
			mock(AbstractResourceEventDemuxer.class),
			mock(ResourceKey.class),
			name,
			rhinoContextProvider,
			mock(ContinuationPendingCache.class),
			mock(MockPendingKeyProvider.class),
			mock(RequireInnerFunction.class),
			mock(InjectFunction.class),
			mock(Timers.class),
			mock(Publisher.class),
			mock(ResourceFinder.class)
		);
	}
	
	public MockClock clock() {
		return (MockClock)clock;
	}
	
	public ResourceConfiguration resourceConfiguration() {
		return resourceConfiguration;
	}
	
	public AbstractResourceEventDemuxer abstractResourceInitializationListener() {
		return demuxer;
	}
	
	public ResourceKey resourceCacheKey() {
		return resourceKey;
	}
	
	public String name() {
		return name;
	}
	
	public MockRhinoContextProvider rhinoContextProvider() {
		return (MockRhinoContextProvider)contextProvider;
	}
	
	public ContinuationPendingCache continuationPendingCache() {
		return continuationPendingCache;
	}
	
	public MockPendingKeyProvider pendingKeyProvider() {
		return (MockPendingKeyProvider)pendingKeyProvider;
	}

	public RequireInnerFunction requireInnerFunction() {
		return requireInnerFunction;
	}
	
	public Timers timers() {
		return timers;
	}
	
	public Publisher publisher() {
		return publisher;
	}
	
	public ResourceFinder resourceFinder() {
		return resourceFinder;
	}
}
