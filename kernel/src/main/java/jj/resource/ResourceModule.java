package jj.resource;

import jj.JJModule;


public class ResourceModule extends JJModule {

	@Override
	protected void configure() {
		
		bindAPIModulePath("/jj/resource/api");
		
		bindConfiguration(ResourceConfiguration.class);

		bindExecutor(ResourceExecutor.class);
		
		bind(PathResolver.class).to(PathResolverImpl.class);
		
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		bind(ResourceLoader.class).to(ResourceLoaderImpl.class);
		
		bindStartupListener(DirectoryStructureLoader.class);
		bindStartupListener(ResourceWatchServiceLoop.class);
		
		bindCreationOf(DirectoryResource.class).to(DirectoryResourceCreator.class);
		
		bindCreationOf(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindLoggedEventsAnnotatedWith(ResourceLogger.class).toLogger(ResourceLogger.NAME);
	}
}
