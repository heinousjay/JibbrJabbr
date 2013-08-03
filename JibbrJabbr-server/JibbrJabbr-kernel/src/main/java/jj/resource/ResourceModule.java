package jj.resource;

import java.io.IOException;

import jj.JJModule;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;


public class ResourceModule extends JJModule {
	
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
		
		addServerListenerBinding().to(AssetResourcePreloader.class);
		addServerListenerBinding().to(ResourceCache.class);
		
		Multibinder<ResourceCreator<? extends Resource>> resourceCreators = 
			Multibinder.newSetBinder(binder(), new TypeLiteral<ResourceCreator<? extends Resource>>() {});
		resourceCreators.addBinding().to(AssetResourceCreator.class);
		resourceCreators.addBinding().to(CssResourceCreator.class);
		resourceCreators.addBinding().to(HtmlResourceCreator.class);
		resourceCreators.addBinding().to(ScriptResourceCreator.class);
		resourceCreators.addBinding().to(StaticResourceCreator.class);
		resourceCreators.addBinding().to(PropertiesResourceCreator.class);
		
		
		// these guys love each other but it's easier to manage the implementation
		// in two classes so i don't feel this is a big burden.  there may be a 
		// better design lurking.
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		if (!isTest) {
			bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
			addServerListenerBinding().to(ResourceWatchService.class);
		} else {
			bind(ResourceWatchService.class).to(TestWatchService.class);
		}
		
	}
}
