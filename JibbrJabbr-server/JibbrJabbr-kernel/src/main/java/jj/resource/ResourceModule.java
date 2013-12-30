package jj.resource;

import java.io.IOException;

import jj.JJModule;
import jj.resource.asset.AssetResource;
import jj.resource.asset.AssetResourceCreator;
import jj.resource.config.ConfigResource;
import jj.resource.config.ConfigResourceCreator;
import jj.resource.css.CssResource;
import jj.resource.css.CssResourceCreator;
import jj.resource.document.DocumentProcessingModule;
import jj.resource.document.HtmlResource;
import jj.resource.document.HtmlResourceCreator;
import jj.resource.property.PropertiesResource;
import jj.resource.property.PropertiesResourceCreator;
import jj.resource.script.ModuleScriptEnvironment;
import jj.resource.script.ModuleScriptEnvironmentCreator;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceCreator;
import jj.resource.sha1.Sha1Resource;
import jj.resource.sha1.Sha1ResourceCreator;
import jj.resource.spec.SpecResource;
import jj.resource.spec.SpecResourceCreator;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceCreator;


public class ResourceModule extends JJModule {
	
	static final class TestWatchService implements ResourceWatchService {

		@Override
		public void watch(FileResource resource) throws IOException {}
		
	}
	
	private final boolean isTest;
	
	public ResourceModule(final boolean isTest) {
		this.isTest = isTest;
	}

	@Override
	protected void configure() {
		
		bind(ResourceCache.class).to(ResourceCacheImpl.class);
		addShutdownListenerBinding().to(ResourceCacheImpl.class);
		
		ResourceCreatorBinder bindCreation = new ResourceCreatorBinder(binder());
		
		bindCreation.of(AssetResource.class).to(AssetResourceCreator.class);
		
		bindCreation.of(ConfigResource.class).to(ConfigResourceCreator.class);
		
		bindCreation.of(CssResource.class).to(CssResourceCreator.class);
		
		bindCreation.of(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindCreation.of(SpecResource.class).to(SpecResourceCreator.class);
		
		bindCreation.of(StaticResource.class).to(StaticResourceCreator.class);
		
		bindCreation.of(PropertiesResource.class).to(PropertiesResourceCreator.class);

		
		bindCreation.of(HtmlResource.class).to(HtmlResourceCreator.class);
		bindCreation.of(ScriptResource.class).to(ScriptResourceCreator.class);
		bindCreation.of(ModuleScriptEnvironment.class).to(ModuleScriptEnvironmentCreator.class);
		
		
		bind(ResourceFinder.class).to(ResourceFinderImpl.class);
		
		if (!isTest) {
			bind(ResourceWatchService.class).to(ResourceWatchServiceImpl.class);
			addStartupListenerBinding().to(ResourceWatchServiceImpl.class);
			addShutdownListenerBinding().to(ResourceWatchServiceImpl.class);
		} else {
			bind(ResourceWatchService.class).to(TestWatchService.class);
		}
		
		install(new DocumentProcessingModule());
		
	}
}
