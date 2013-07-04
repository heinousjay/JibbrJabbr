package jj.servable;

import jj.JJModule;

import com.google.inject.multibindings.Multibinder;

public class ServableModule extends JJModule {

	@Override
	protected void configure() {
		Multibinder<Servable> servables = Multibinder.newSetBinder(binder(), Servable.class);
		
		// DocumentServable is always first
		servables.addBinding().to(DocumentServable.class);
		
		servables.addBinding().to(AssociatedScriptServable.class);
		
		servables.addBinding().to(CssServable.class);
		
		// StaticServable is always second last
		servables.addBinding().to(StaticServable.class);
		
		// AssetServable is always last
		servables.addBinding().to(AssetServable.class);
	}
}
