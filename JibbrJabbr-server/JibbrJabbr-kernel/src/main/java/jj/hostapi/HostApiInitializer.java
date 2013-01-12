package jj.hostapi;

import static org.picocontainer.Characteristics.HIDE_IMPL;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.parameters.ComponentParameter;
import org.picocontainer.parameters.ConstantParameter;

/**
 * Prepares all script host related objects in our central container
 * @author jason
 *
 */
public class HostApiInitializer {

	public static MutablePicoContainer initialize(MutablePicoContainer container) {
		
		container.addComponent(ClientStorage.class)
			.addComponent(RestCallProvider.class)
			.addComponent(RestServiceFunction.class)
			.addComponent(DollarFunction.class)
			.addComponent(DoCallFunction.class)
			.addComponent(DoInvokeFunction.class)
			.addComponent(DoRetrieveFunction.class)
			.addComponent(DoStoreFunction.class)
			.addComponent(PrepareConnectionIteratorFunction.class)
			.addComponent(NextConnectionFunction.class)
			.addComponent(PrintFunction.class)
			.addComponent(PrintfFunction.class)
			.addComponent(RequireFunction.class)
			.addComponent(ScriptJSON.class)
			// the rhino object creator participates in some circular dependencies,
			// so it needs the hidden implementation in order to allow construction
			// to proceed
			.as(HIDE_IMPL).addComponent(RhinoObjectCreator.class, RhinoObjectCreatorImpl.class);
		
		// special registration for hostEvents
		for (HostEvent event : HostEvent.values()) {
			container.addComponent(
				event, 
				EventRegistrationFunction.class, 
				new ConstantParameter(event.toString()),
				new ComponentParameter()
			);
		}
		
		return container;
	}
}
