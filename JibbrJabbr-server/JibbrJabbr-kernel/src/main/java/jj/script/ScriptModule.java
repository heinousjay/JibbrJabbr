package jj.script;

import jj.JJModule;

import com.google.inject.multibindings.Multibinder;


public class ScriptModule extends JJModule {

	@Override
	protected void configure() {
		
		Multibinder<ContinuationProcessor> continuationProcessors = 
			Multibinder.newSetBinder(binder(), ContinuationProcessor.class);
		continuationProcessors.addBinding().to(JQueryMessageContinuationProcessor.class);
		continuationProcessors.addBinding().to(RestRequestContinuationProcessor.class);
		continuationProcessors.addBinding().to(RequiredModuleContinuationProcessor.class);
		
	}
}
