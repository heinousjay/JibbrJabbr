package jj.http.server.websocket;

import static org.mockito.Mockito.mock;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.ResourceFinder;
import jj.resource.ResourceKey;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.MockRhinoContextProvider;

public class MockAbstractWebSocketConnectionHostDependencies extends AbstractWebSocketConnectionHost.Dependencies {

	public MockAbstractWebSocketConnectionHostDependencies(String name, ResourceFinder resourceFinder) {
		super(
			new MockAbstractResourceDependencies.MockInnerDependencies(resourceFinder),
			new MockAbstractScriptEnvironmentDependencies.MockInnerAbstractScriptEnvironmentDependenciesDependencies(),
			mock(ResourceKey.class),
			name
		);
	}

	public MockRhinoContextProvider mockRhinoContextProvider() {
		return ((MockAbstractScriptEnvironmentDependencies.MockInnerAbstractScriptEnvironmentDependenciesDependencies)scriptEnvironmentDependencies).mockRhinoContextProvider();
	}

	public ResourceKey cacheKey() {
		return resourceKey;
	}
}
