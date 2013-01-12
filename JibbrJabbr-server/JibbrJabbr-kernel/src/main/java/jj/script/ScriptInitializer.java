package jj.script;

import org.picocontainer.MutablePicoContainer;

public class ScriptInitializer {

	public static MutablePicoContainer initialize(final MutablePicoContainer container) {
		return container
			.addComponent(ContinuationCoordinator.class)
			.addComponent(JQueryMessageContinuationProcessor.class)
			.addComponent(RestRequestContinuationProcessor.class)
			.addComponent(RequiredModuleContinuationProcessor.class)
			.addComponent(ScriptExecutorFactory.class)
			.addComponent(CurrentScriptContext.class)
			.addComponent(ScriptBundleCreator.class)
			.addComponent(ScriptBundleFinder.class)
			.addComponent(ScriptBundleHelper.class)
			.addComponent(ScriptBundles.class)
			.addComponent(ScriptRunner.class);
	}
}
