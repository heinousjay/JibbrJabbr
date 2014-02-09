package jj.script;

import jj.JJModule;
import jj.jjmessage.JJMessage;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;


public class ScriptModule extends JJModule {

	@Override
	protected void configure() {
		
		bind(ScriptRunner.class).to(ScriptRunnerImpl.class);
		bind(ScriptRunnerInternal.class).to(ScriptRunnerImpl.class);
		
		MapBinder<Class<? extends Continuable>, ContinuationProcessor> processors =
			MapBinder.newMapBinder(
				binder(),
				new TypeLiteral<Class<? extends Continuable>>() {},
				new TypeLiteral<ContinuationProcessor>() {}
			);
		
		processors.addBinding(RestRequest.class).to(RestRequestContinuationProcessor.class);
		processors.addBinding(JJMessage.class).to(JJMessageContinuationProcessor.class);
		processors.addBinding(RequiredModule.class).to(RequiredModuleContinuationProcessor.class);
	}
}
