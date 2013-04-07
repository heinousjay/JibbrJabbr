package jj.servable;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ServableModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<Servable> servables = Multibinder.newSetBinder(binder(), Servable.class);
		
		// HtmlServable is always first
		servables.addBinding().to(HtmlServable.class);
		
		servables.addBinding().to(AssociatedScriptServable.class);
		
		servables.addBinding().to(SocketServable.class);
		
		//.addComponent(CssServable.class)
		
		// StaticServable is always second last
		servables.addBinding().to(StaticServable.class);
		
		// AssetServable is always last
		servables.addBinding().to(AssetServable.class);
	}
}
