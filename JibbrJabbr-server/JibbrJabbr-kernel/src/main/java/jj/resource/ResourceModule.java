package jj.resource;

import java.io.IOException;

import jj.JJServerListener;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;


public class ResourceModule extends AbstractModule {
	
	static final class TestWatchService implements ResourceWatchService {

		@Override
		public void start() throws Exception {}

		@Override
		public void stop() {}

		@Override
		public void watch(Resource resource) throws IOException {}
		
	}
	
	private final boolean isTest;
	
	public ResourceModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		bind(ResourceCache.class);
		
		Multibinder<ResourceCreator<?>> resourceCreators = Multibinder.newSetBinder(binder(), new TypeLiteral<ResourceCreator<?>>() {});
		resourceCreators.addBinding().to(AssetResourceCreator.class);
		resourceCreators.addBinding().to(HtmlResourceCreator.class);
		resourceCreators.addBinding().to(ScriptResourceCreator.class);
		resourceCreators.addBinding().to(StaticResourceCreator.class);
		resourceCreators.addBinding().to(PropertiesResourceCreator.class);
		
		// these guys love each other but it's easier to manage the implementation
		// in two classes so i don't feel this is a big burden
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		if (!isTest) {
			bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
		} else {
			bind(ResourceWatchService.class).to(TestWatchService.class);
		}
		
		Multibinder<JJServerListener> serverListeners = Multibinder.newSetBinder(binder(), JJServerListener.class);
		if (!isTest) serverListeners.addBinding().to(ResourceWatchService.class);
		serverListeners.addBinding().to(AssetResourcePreloader.class);
	}
}
