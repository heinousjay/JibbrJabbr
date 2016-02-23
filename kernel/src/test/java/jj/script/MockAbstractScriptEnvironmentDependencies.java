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

import static jj.server.ServerLocation.Virtual;
import static org.mockito.Mockito.mock;

import javax.inject.Provider;

import jj.event.MockPublisher;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.MockResourceIdentifierMaker;
import jj.resource.ResourceFinder;
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
				mock(ContinuationCoordinator.class),
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
	
	public interface MockPendingKeyProvider extends Provider<PendingKey> {}

	public <T extends ScriptEnvironment<Void>> MockAbstractScriptEnvironmentDependencies(
		Class<T> environmentType,
		String name
	) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(),
			new MockInnerAbstractScriptEnvironmentDependencies(),
			new MockResourceIdentifierMaker().make(environmentType, Virtual, name, null)
		);
	}

	public <T extends ScriptEnvironment<Void>> MockAbstractScriptEnvironmentDependencies(
		Class<T> environmentType,
		String name,
		ResourceFinder resourceFinder
	) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(resourceFinder),
			new MockInnerAbstractScriptEnvironmentDependencies(),
			new MockResourceIdentifierMaker().make(environmentType, Virtual, name, null)
		);
	}

	public <T extends ScriptEnvironment<Void>> MockAbstractScriptEnvironmentDependencies(
		Class<T> environmentType,
		String name,
		RealRhinoContextProvider rhinoContextProvider
	) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(),
			new MockInnerAbstractScriptEnvironmentDependencies(rhinoContextProvider),
			new MockResourceIdentifierMaker().make(environmentType, Virtual, name, null)
		);
	}

	public <A, T extends ScriptEnvironment<A>> MockAbstractScriptEnvironmentDependencies(
		Class<T> environmentType,
		String name,
		A argument
	) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(),
			new MockInnerAbstractScriptEnvironmentDependencies(),
			new MockResourceIdentifierMaker().make(environmentType, Virtual, name, argument)
		);
	}

	public <A, T extends ScriptEnvironment<A>> MockAbstractScriptEnvironmentDependencies(
		Class<T> environmentType,
		String name,
		A argument,
		RealRhinoContextProvider rhinoContextProvider
	) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(),
			new MockInnerAbstractScriptEnvironmentDependencies(rhinoContextProvider),
			new MockResourceIdentifierMaker().make(environmentType, Virtual, name, argument)
		);
	}
	
	public ContinuationCoordinator continuationCoordinator() {
		return scriptEnvironmentDependencies.continuationCoordinator;
	}
	
	public MockPublisher publisher() {
		return ((MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies)abstractResourceDependencies).publisher();
	}

	public ResourceFinder resourceFinder() {
		return ((MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies)abstractResourceDependencies).resourceFinder();
	}

	public MockRhinoContextProvider mockRhinoContextProvider() {
		return (MockRhinoContextProvider)scriptEnvironmentDependencies.contextProvider;
	}
}
