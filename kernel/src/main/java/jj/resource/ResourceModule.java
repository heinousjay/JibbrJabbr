package jj.resource;

import jj.JJModule;
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
		
		addStartupListenerBinding().to(DirectoryStructureLoader.class);
		
		bindCreation().of(DirectoryResource.class).to(DirectoryResourceCreator.class);
		
		bindCreation().of(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindCreation().of(StaticResource.class).to(StaticResourceCreator.class);
		
		bindLoggedEvents().annotatedWith(ResourceLogger.class).toLogger(ResourceLogger.NAME);
	}
}
