package jj.http.server.websocket;

import static jj.server.ServerLocation.Virtual;
import jj.resource.MockAbstractResourceDependencies;
import jj.resource.MockResourceIdentifierMaker;
import jj.resource.ResourceFinder;
import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.MockRhinoContextProvider;

public class MockAbstractWebSocketConnectionHostDependencies extends AbstractWebSocketConnectionHost.Dependencies {
	
	public static class MockInnerAbstractWebSocketConnectionHostDependencies 
		extends AbstractWebSocketConnectionHost.AbstractWebSocketConnectionHostDependencies {
		
	}

	public <T extends WebSocketConnectionHost> MockAbstractWebSocketConnectionHostDependencies(
		Class<T> resourceClass,
		String name,
		ResourceFinder resourceFinder
	) {
		super(
			new MockAbstractResourceDependencies.MockInnerAbstractResourceDependencies(resourceFinder),
			new MockAbstractScriptEnvironmentDependencies.MockInnerAbstractScriptEnvironmentDependencies(),
			new MockInnerAbstractWebSocketConnectionHostDependencies(),
			new MockResourceIdentifierMaker().make(resourceClass, Virtual, name)
		);
	}

	public MockRhinoContextProvider mockRhinoContextProvider() {
		return ((MockAbstractScriptEnvironmentDependencies.MockInnerAbstractScriptEnvironmentDependencies)scriptEnvironmentDependencies).mockRhinoContextProvider();
	}

}
