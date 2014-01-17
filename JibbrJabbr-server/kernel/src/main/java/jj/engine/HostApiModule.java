package jj.engine;

import jj.JJModule;

import com.google.inject.multibindings.Multibinder;

/**
 * Prepares all script host related objects in our central container
 * @author jason
 *
 */
public class HostApiModule extends JJModule {

	@Override
	protected void configure() {
		
		bind(EngineAPI.class).to(EngineAPIImpl.class);
		
		Multibinder<HostObject> hostObjects = Multibinder.newSetBinder(binder(), HostObject.class);
		hostObjects.addBinding().to(ClientStorage.class);
		hostObjects.addBinding().to(RestServiceFunction.class);
		hostObjects.addBinding().to(DollarFunction.class);
		hostObjects.addBinding().to(DoCallFunction.class);
		hostObjects.addBinding().to(DoInvokeFunction.class);
		hostObjects.addBinding().to(DoRetrieveFunction.class);
		hostObjects.addBinding().to(DoStoreFunction.class);
		hostObjects.addBinding().to(PrepareConnectionIteratorFunction.class);
		hostObjects.addBinding().to(NextConnectionFunction.class);
		hostObjects.addBinding().to(PrintFunction.class);
		hostObjects.addBinding().to(PrintfFunction.class);
		hostObjects.addBinding().to(MakeRequireFunction.class);
		hostObjects.addBinding().to(ClientConnectedFunction.class);
		hostObjects.addBinding().to(ClientDisconnectedFunction.class);
		hostObjects.addBinding().to(TerminatingFunction.class);
	}

}
