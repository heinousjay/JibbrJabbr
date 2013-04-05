package jj.resource;

import jj.JJServerListener;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;


public class ResourceModule extends AbstractModule {

	@Override
	protected void configure() {
		
		bind(ResourceCache.class);
		
		Multibinder<ResourceCreator<?>> resourceCreators = Multibinder.newSetBinder(binder(), new TypeLiteral<ResourceCreator<?>>() {});
		resourceCreators.addBinding().to(AssetResourceCreator.class);
		resourceCreators.addBinding().to(HtmlResourceCreator.class);
		resourceCreators.addBinding().to(ScriptResourceCreator.class);
		resourceCreators.addBinding().to(PropertiesResourceCreator.class);
		
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		Multibinder<JJServerListener> serverListeners = Multibinder.newSetBinder(binder(), JJServerListener.class);
		serverListeners.addBinding().to(ResourceWatchService.class);
		serverListeners.addBinding().to(AssetResourcePreloader.class);
	}
}
