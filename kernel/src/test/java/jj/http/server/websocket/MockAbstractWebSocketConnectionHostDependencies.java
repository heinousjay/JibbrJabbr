package jj.http.server.websocket;

import static org.mockito.Mockito.mock;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.ResourceFinder;
import jj.resource.ResourceKey;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.MockRhinoContextProvider;

public class MockAbstractWebSocketConnectionHostDependencies extends AbstractWebSocketConnectionHost.Dependencies {
	
	public static class MockInnerAbstractWebSocketConnectionHostDependencies 
		extends AbstractWebSocketConnectionHost.AbstractWebSocketConnectionHostDependencies {
		
	}

	public MockAbstractWebSocketConnectionHostDependencies(String name, ResourceFinder resourceFinder) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(resourceFinder),
			new MockAbstractScriptEnvironmentDependencies.MockInnerAbstractScriptEnvironmentDependencies(),
			new MockInnerAbstractWebSocketConnectionHostDependencies(),
			mock(ResourceKey.class),
			name
		);
	}

	public MockRhinoContextProvider mockRhinoContextProvider() {
		return ((MockAbstractScriptEnvironmentDependencies.MockInnerAbstractScriptEnvironmentDependencies)scriptEnvironmentDependencies).mockRhinoContextProvider();
	}

	public ResourceKey cacheKey() {
		return resourceKey;
	}
}
