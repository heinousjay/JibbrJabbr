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
		super(
			new MockClock(),
			mock(ResourceConfiguration.class),
			mock(ResourceKey.class),
			new MockRhinoContextProvider(),
			mock(MockPendingKeyProvider.class),
			mock(RequireInnerFunction.class),
			mock(InjectFunction.class),
			mock(Timers.class),
			mock(Publisher.class),
			mock(ResourceFinder.class)
		);
	}

	public MockAbstractScriptEnvironmentDependencies(RealRhinoContextProvider rhinoContextProvider) {
		super(
			new MockClock(),
			mock(ResourceConfiguration.class),
			mock(ResourceKey.class),
			rhinoContextProvider,
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
	
	public ResourceKey resourceCacheKey() {
		return resourceKey;
	}
	
	public MockRhinoContextProvider rhinoContextProvider() {
		return (MockRhinoContextProvider)contextProvider;
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
