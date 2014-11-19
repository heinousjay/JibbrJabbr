package jj.script;

import org.mozilla.javascript.ScriptableObject;

import jj.JJModule;
import jj.script.module.ScriptResourceModule;


public class ScriptModule extends JJModule {
	
	@Override
	protected void configure() {
		
		addAPIModulePath("/jj/script/api");
		
		bindConfiguration().to(ScriptExecutionConfiguration.class);
		
		bind(DependsOnScriptEnvironmentInitialization.class).to(ScriptEnvironmentInitializer.class);
		
		bind(ContinuationCoordinator.class).to(ContinuationCoordinatorImpl.class);
		
		bindExecutor(ScriptExecutorFactory.class);
		
		install(new ScriptResourceModule());
		
		bind(ScriptableObject.class).annotatedWith(Global.class).toProvider(GlobalStandardObjects.class);
		
		bindLoggedEvents().annotatedWith(ScriptSystemLogger.class).toLogger(ScriptSystemLogger.NAME);
	}
}
