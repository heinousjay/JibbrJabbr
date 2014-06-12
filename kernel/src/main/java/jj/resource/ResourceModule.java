package jj.resource;

import jj.JJModule;
import jj.resource.config.ConfigResource;
import jj.resource.config.ConfigResourceCreator;
import jj.resource.sha1.Sha1Resource;
import jj.resource.sha1.Sha1ResourceCreator;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceCreator;


public class ResourceModule extends JJModule {
	
	public ResourceModule() {
	}

	@Override
	protected void configure() {
		
		addAPIModulePath("/jj/resource/api");
		
		bindConfiguration().to(ResourceConfiguration.class);

		bindExecutor(ResourceExecutor.class);
		
		bind(ResourceCache.class).to(ResourceCacheImpl.class);
		
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		bind(ResourceLoader.class).to(ResourceLoaderImpl.class);
		
		bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
		addStartupListenerBinding().to(ResourceWatchServiceImpl.class);
		
		bindCreation().of(ConfigResource.class).to(ConfigResourceCreator.class);
		
		bindCreation().of(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindCreation().of(StaticResource.class).to(StaticResourceCreator.class);
		
		bindLoggedEvents().annotatedWith(ResourceLogger.class).toLogger(ResourceLogger.NAME);
	}
}
