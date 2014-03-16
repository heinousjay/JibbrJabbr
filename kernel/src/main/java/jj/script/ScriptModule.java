package jj.script;

import jj.JJModule;
import jj.script.resource.ScriptResourceModule;


public class ScriptModule extends JJModule {
	
	@Override
	protected void configure() {
		
		bind(DependsOnScriptEnvironmentInitialization.class).to(ScriptEnvironmentInitializer.class);
		
		bind(ContinuationCoordinator.class).to(ContinuationCoordinatorImpl.class);
		
		bindExecutor(ScriptExecutorFactory.class);
		
		install(new ScriptResourceModule());
	}
}
