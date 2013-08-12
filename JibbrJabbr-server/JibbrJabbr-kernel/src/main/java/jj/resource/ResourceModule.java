package jj.resource;

import java.io.IOException;

import jj.JJModule;

import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;


public class ResourceModule extends JJModule {
	
	static final class TestWatchService implements ResourceWatchService {

		@Override
		public void watch(Resource resource) throws IOException {}
		
	}
	
	private final boolean isTest;
	
	public ResourceModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		bind(ResourceCache.class).to(ResourceCacheImpl.class);
		addShutdownListenerBinding().to(ResourceCacheImpl.class);
		
		MapBinder<Class<? extends Resource>, ResourceCreator<? extends Resource>> resourceCreatorBinder = 
			MapBinder.newMapBinder(
				binder(),
				new TypeLiteral<Class<? extends Resource>>() {},
				new TypeLiteral<ResourceCreator<? extends Resource>>() {}
			);
		resourceCreatorBinder.addBinding(AssetResource.class).to(AssetResourceCreator.class);
		resourceCreatorBinder.addBinding(ConfigResource.class).to(ConfigResourceCreator.class);
		resourceCreatorBinder.addBinding(CssResource.class).to(CssResourceCreator.class);
		resourceCreatorBinder.addBinding(HtmlResource.class).to(HtmlResourceCreator.class);
		resourceCreatorBinder.addBinding(ScriptResource.class).to(ScriptResourceCreator.class);
		resourceCreatorBinder.addBinding(Sha1Resource.class).to(Sha1ResourceCreator.class);
		resourceCreatorBinder.addBinding(StaticResource.class).to(StaticResourceCreator.class);
		resourceCreatorBinder.addBinding(PropertiesResource.class).to(PropertiesResourceCreator.class);
		
		
		// these guys love each other but it's easier to manage the implementation
		// in two classes so i don't feel this is a big burden.  there may be a 
		// better design lurking.
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		if (!isTest) {
			bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
			addStartupListenerBinding().to(ResourceWatchServiceImpl.class);
			addShutdownListenerBinding().to(ResourceWatchServiceImpl.class);
		} else {
			bind(ResourceWatchService.class).to(TestWatchService.class);
		}
		
	}
}
