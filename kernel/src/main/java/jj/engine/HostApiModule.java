package jj.engine;

import jj.JJModule;
/**
 * Prepares all script host related objects in our central container
 * @author jason
 *
 */
public class HostApiModule extends JJModule implements BindsEngineHostObject {

	@Override
	protected void configure() {
		
		bind(EngineAPI.class).to(EngineAPIImpl.class);
		
		bindHostObject(ClientStorage.class);
		bindHostObject(DollarFunction.class);
		bindHostObject(DoCallFunction.class);
		bindHostObject(DoInvokeFunction.class);
		bindHostObject(ClientConnectedFunction.class);
		bindHostObject(ClientDisconnectedFunction.class);
		bindHostObject(TerminatingFunction.class);
	}

}
