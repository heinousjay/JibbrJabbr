package jj.script;

import static jj.script.ContinuationType.*;

import jj.JJModule;

import com.google.inject.multibindings.MapBinder;


public class ScriptModule extends JJModule {

	@Override
	protected void configure() {
		
		bind(ScriptRunner.class).to(ScriptRunnerImpl.class);
		bind(ScriptRunnerInternal.class).to(ScriptRunnerImpl.class);
		
		MapBinder<ContinuationType, ContinuationProcessor> processors =
			MapBinder.newMapBinder(binder(), ContinuationType.class, ContinuationProcessor.class);
		
		processors.addBinding(AsyncHttpRequest).to(RestRequestContinuationProcessor.class);
		processors.addBinding(JJMessage).to(JJMessageContinuationProcessor.class);
		processors.addBinding(RequiredModule).to(RequiredModuleContinuationProcessor.class);
	}
}
