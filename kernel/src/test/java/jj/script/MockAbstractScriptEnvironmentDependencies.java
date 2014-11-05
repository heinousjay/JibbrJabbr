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

import jj.event.MockPublisher;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.ResourceFinder;
import jj.resource.ResourceKey;
import jj.script.AbstractScriptEnvironment.AbstractScriptEnvironmentDependencies;

/**
 * @author jason
 *
 */
public class MockAbstractScriptEnvironmentDependencies extends AbstractScriptEnvironment.Dependencies {
	
	public static class MockInnerAbstractScriptEnvironmentDependencies extends AbstractScriptEnvironmentDependencies {

		public MockInnerAbstractScriptEnvironmentDependencies() {
			this(new MockRhinoContextProvider());
		}
		
		MockInnerAbstractScriptEnvironmentDependencies(Provider<RhinoContext> rhinoContextProvider) {
			super(
				mock(ContinuationPendingCache.class),
				mock(MockPendingKeyProvider.class),
				mock(RequireInnerFunction.class),
				mock(InjectFunction.class),
				mock(Timers.class),
				rhinoContextProvider
			);
			
		}
		
		public MockRhinoContextProvider mockRhinoContextProvider() {
			return (MockRhinoContextProvider)contextProvider;
		}
	}
	
	public interface MockPendingKeyProvider extends Provider<ContinuationPendingKey> {}
	
	public MockAbstractScriptEnvironmentDependencies() {
		this("unnamed");
	}

	public MockAbstractScriptEnvironmentDependencies(final String name) {
		this(name, mock(ResourceFinder.class));
	}

	public MockAbstractScriptEnvironmentDependencies(final String name, final ResourceFinder resourceFinder) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(resourceFinder),
			new MockInnerAbstractScriptEnvironmentDependencies(),
			mock(ResourceKey.class),
			name
		);
	}

	public MockAbstractScriptEnvironmentDependencies(final RealRhinoContextProvider rhinoContextProvider, final String name) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(),
			new MockInnerAbstractScriptEnvironmentDependencies(rhinoContextProvider),
			mock(ResourceKey.class),
			name
		);
	}
	
	public MockPublisher publisher() {
		return ((MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies)abstractResourceDependencies).publisher();
	}

	public ResourceKey cacheKey() {
		return resourceKey;
	}

	public ResourceFinder resourceFinder() {
		return ((MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies)abstractResourceDependencies).resourceFinder();
	}

	public MockRhinoContextProvider mockRhinoContextProvider() {
		return (MockRhinoContextProvider)scriptEnvironmentDependencies.contextProvider;
	}
}
