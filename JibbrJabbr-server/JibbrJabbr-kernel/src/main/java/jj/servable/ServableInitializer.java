package jj.servable;

import org.picocontainer.MutablePicoContainer;

public class ServableInitializer {

	public static MutablePicoContainer initialize(MutablePicoContainer container) {
		return container
			// these are only ever looked up by the Servable base
			//.addComponent(CssServable.class)
			.addComponent(HtmlServable.class)
			.addComponent(AssociatedScriptServable.class)
			.addComponent(SocketServable.class)
			.addComponent(AssetServable.class);
	}
}
