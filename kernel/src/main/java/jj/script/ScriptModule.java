package jj.script;

import jj.configuration.BindsConfiguration;
import jj.execution.BindsExecutor;
import jj.logging.BindsLogger;
import jj.server.BindsServerPath;
import org.mozilla.javascript.ScriptableObject;

import jj.JJModule;
import jj.script.module.ScriptResourceModule;


public class ScriptModule extends JJModule
	implements BindsConfiguration,
		BindsExecutor,
		BindsLogger,
	BindsServerPath {
	
	@Override
	protected void configure() {
		
		bindAPIModulePath("/jj/script/api");
		
		bindConfiguration(ScriptExecutionConfiguration.class);
		
		bind(DependsOnScriptEnvironmentInitialization.class).to(ScriptEnvironmentInitializer.class);
		
		bind(ContinuationResumer.class).to(ContinuationCoordinator.class);
		
		bindExecutor(ScriptExecutor.class);
		
		install(new ScriptResourceModule());
		
		bind(ScriptableObject.class).annotatedWith(Global.class).toProvider(GlobalStandardObjects.class);
		
		bindLoggedEventsAnnotatedWith(ScriptSystemLogger.class).toLogger(ScriptSystemLogger.NAME);
	}
}
