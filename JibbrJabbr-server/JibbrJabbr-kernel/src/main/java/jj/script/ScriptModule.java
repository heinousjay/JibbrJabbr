package jj.script;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;


public class ScriptModule extends AbstractModule {

	@Override
	protected void configure() {
		
		Multibinder<ContinuationProcessor> continuationProcessors = Multibinder.newSetBinder(binder(), ContinuationProcessor.class);
		continuationProcessors.addBinding().to(JQueryMessageContinuationProcessor.class);
		continuationProcessors.addBinding().to(RestRequestContinuationProcessor.class);
		continuationProcessors.addBinding().to(RequiredModuleContinuationProcessor.class);
		
		bind(ContinuationCoordinator.class);
		bind(ScriptExecutorFactory.class);
		bind(CurrentScriptContext.class);
		bind(ScriptBundleCreator.class);
		bind(ScriptBundleFinder.class);
		bind(ScriptBundleHelper.class);
		bind(ScriptBundles.class);
		bind(ScriptRunner.class);
	}

	/*
	public static MutablePicoContainer initialize(final MutablePicoContainer container) {
		return container

	}
	*/
}
