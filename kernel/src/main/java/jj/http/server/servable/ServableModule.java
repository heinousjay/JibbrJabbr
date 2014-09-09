package jj.http.server.servable;

import jj.JJModule;
import jj.http.server.ServableResource;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

public class ServableModule extends JJModule {

	@Override
	protected void configure() {
		Multibinder<Servable<? extends ServableResource>> servables = 
			Multibinder.newSetBinder(binder(), new TypeLiteral<Servable<? extends ServableResource>>() {});
		
		// DocumentServable is always first
		servables.addBinding().to(DocumentServable.class);
		
		servables.addBinding().to(DocumentScriptServable.class);
		
		servables.addBinding().to(CssServable.class);
		
		// StaticServable is always second last
		servables.addBinding().to(StaticServable.class);
	}
}
