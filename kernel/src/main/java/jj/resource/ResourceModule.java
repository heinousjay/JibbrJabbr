package jj.resource;

import jj.JJModule;
import jj.configuration.BindsConfiguration;
import jj.execution.BindsExecutor;
import jj.logging.BindsLogger;
import jj.server.BindsServerPath;


public class ResourceModule extends JJModule
	implements BindsConfiguration,
		BindsExecutor,
		BindsLogger,
		BindsResourceCreation,
	BindsServerPath {

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
		
		createResource(DirectoryResource.class).using(DirectoryResourceCreator.class);

		createResource(Sha1Resource.class).using(Sha1ResourceCreator.class);
		
		bindLoggedEventsAnnotatedWith(ResourceLogger.class).toLogger(ResourceLogger.NAME);
	}
}
