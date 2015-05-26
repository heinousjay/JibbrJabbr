package jj.resource;

import jj.JJModule;


public class ResourceModule extends JJModule {
	
	public ResourceModule() {
	}

	@Override
	protected void configure() {
		
		bindAPIModulePath("/jj/resource/api");
		
		bindConfiguration(ResourceConfiguration.class);

		bindExecutor(ResourceExecutor.class);
		
		bind(PathResolver.class).to(PathResolverImpl.class);
		
		bind(ResourceCache.class).to(ResourceCacheImpl.class);
		
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		bind(ResourceLoader.class).to(ResourceLoaderImpl.class);
		
		bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
		
		bindStartupListener(DirectoryStructureLoader.class);
		
		bindCreationOf(DirectoryResource.class).to(DirectoryResourceCreator.class);
		
		bindCreationOf(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindLoggedEventsAnnotatedWith(ResourceLogger.class).toLogger(ResourceLogger.NAME);
	}
}
