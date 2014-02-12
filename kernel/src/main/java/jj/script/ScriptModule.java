package jj.script;

import jj.JJModule;
import jj.jjmessage.JJMessage;


public class ScriptModule extends JJModule {
	
	@Override
	protected void configure() {
		
		bind(DependsOnScriptEnvironmentInitialization.class).to(ScriptEnvironmentInitializer.class);
		
		bind(ContinuationCoordinator.class).to(ContinuationCoordinatorImpl.class);
		
		dispatch().continuationOf(RestRequest.class).to(RestRequestContinuationProcessor.class);
		dispatch().continuationOf(JJMessage.class).to(JJMessageContinuationProcessor.class);
		
	}
}
