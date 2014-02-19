package jj.engine;

import jj.JJModule;
/**
 * Prepares all script host related objects in our central container
 * @author jason
 *
 */
public class HostApiModule extends JJModule {

	@Override
	protected void configure() {
		
		bind(EngineAPI.class).to(EngineAPIImpl.class);
		
		addHostObjectBinding().to(ClientStorage.class);
		addHostObjectBinding().to(RestServiceFunction.class);
		addHostObjectBinding().to(DollarFunction.class);
		addHostObjectBinding().to(DoCallFunction.class);
		addHostObjectBinding().to(DoInvokeFunction.class);
		addHostObjectBinding().to(DoRetrieveFunction.class);
		addHostObjectBinding().to(DoStoreFunction.class);
		addHostObjectBinding().to(BroadcastFunction.class);
		addHostObjectBinding().to(PrintFunction.class);
		addHostObjectBinding().to(PrintfFunction.class);
		addHostObjectBinding().to(ClientConnectedFunction.class);
		addHostObjectBinding().to(ClientDisconnectedFunction.class);
		addHostObjectBinding().to(TerminatingFunction.class);
	}

}
