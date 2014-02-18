package jj.resource;

import jj.JJModule;
import jj.resource.config.ConfigResource;
import jj.resource.config.ConfigResourceCreator;
import jj.resource.document.DocumentProcessingModule;
import jj.resource.property.PropertiesResource;
import jj.resource.property.PropertiesResourceCreator;
import jj.resource.script.ScriptResourceModule;
import jj.resource.sha1.Sha1Resource;
import jj.resource.sha1.Sha1ResourceCreator;
import jj.resource.spec.SpecResource;
import jj.resource.spec.SpecResourceCreator;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceCreator;


public class ResourceModule extends JJModule {
	
	public ResourceModule() {
	}

	@Override
	protected void configure() {

		bindTaskRunner().toExecutor(ResourceExecutor.class);
		addShutdownListenerBinding().to(ResourceExecutor.class);
		
		bind(ResourceCache.class).to(ResourceCacheImpl.class);
		addShutdownListenerBinding().to(ResourceCacheImpl.class);
		
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
		addStartupListenerBinding().to(ResourceWatchServiceImpl.class);
		
		addShutdownListenerBinding().to(ResourceWatcher.class);
		
		
		
		bindCreation().of(ConfigResource.class).to(ConfigResourceCreator.class);
		
		bindCreation().of(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindCreation().of(SpecResource.class).to(SpecResourceCreator.class);
		
		bindCreation().of(StaticResource.class).to(StaticResourceCreator.class);
		
		bindCreation().of(PropertiesResource.class).to(PropertiesResourceCreator.class);
		
		
		
		
		install(new DocumentProcessingModule());
		install(new ScriptResourceModule());
		
	}
}
