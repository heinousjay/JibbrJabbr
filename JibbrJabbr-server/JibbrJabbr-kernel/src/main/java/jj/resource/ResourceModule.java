package jj.resource;

import java.io.IOException;

import jj.JJModule;
import jj.resource.asset.AssetResource;
import jj.resource.asset.AssetResourceCreator;
import jj.resource.config.ConfigResource;
import jj.resource.config.ConfigResourceCreator;
import jj.resource.css.CssResource;
import jj.resource.css.CssResourceCreator;
import jj.resource.document.DocumentScriptEnvironment;
import jj.resource.document.DocumentScriptEnvironmentCreator;
import jj.resource.document.HtmlResource;
import jj.resource.document.HtmlResourceCreator;
import jj.resource.document.ModuleScriptEnvironment;
import jj.resource.document.ModuleScriptEnvironmentCreator;
import jj.resource.document.ScriptResource;
import jj.resource.document.ScriptResourceCreator;
import jj.resource.property.PropertiesResource;
import jj.resource.property.PropertiesResourceCreator;
import jj.resource.sha1.Sha1Resource;
import jj.resource.sha1.Sha1ResourceCreator;
import jj.resource.spec.SpecResource;
import jj.resource.spec.SpecResourceCreator;
import jj.resource.stat.ic.StaticResource;
import jj.resource.stat.ic.StaticResourceCreator;

import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;


public class ResourceModule extends JJModule {
	
	static final class TestWatchService implements ResourceWatchService {

		@Override
		public void watch(FileResource resource) throws IOException {}
		
	}
	
	private final boolean isTest;
	
	public ResourceModule(final boolean isTest) {
		this.isTest = isTest;
	}
	
	private MapBinder<Class<? extends Resource>, ResourceCreator<? extends Resource>> resourceCreatorBinder;
	
	@SuppressWarnings("unchecked")
	protected <T extends Resource, U extends ResourceCreator<T>> LinkedBindingBuilder<U> bindCreationOf(Class<T> key) {
		if (resourceCreatorBinder == null) {
			resourceCreatorBinder = 
				MapBinder.newMapBinder(
					binder(),
					new TypeLiteral<Class<? extends Resource>>() {},
					new TypeLiteral<ResourceCreator<? extends Resource>>() {}
				);
		}
		
		return (LinkedBindingBuilder<U>)resourceCreatorBinder.addBinding(key);
	}

	@Override
	protected void configure() {
		
		bind(ResourceCache.class).to(ResourceCacheImpl.class);
		addShutdownListenerBinding().to(ResourceCacheImpl.class);
		
		bindCreationOf(AssetResource.class).to(AssetResourceCreator.class);
		
		bindCreationOf(ConfigResource.class).to(ConfigResourceCreator.class);
		
		bindCreationOf(CssResource.class).to(CssResourceCreator.class);
		
		bindCreationOf(Sha1Resource.class).to(Sha1ResourceCreator.class);
		
		bindCreationOf(SpecResource.class).to(SpecResourceCreator.class);
		
		bindCreationOf(StaticResource.class).to(StaticResourceCreator.class);
		
		bindCreationOf(PropertiesResource.class).to(PropertiesResourceCreator.class);

		
		bindCreationOf(HtmlResource.class).to(HtmlResourceCreator.class);
		bindCreationOf(ScriptResource.class).to(ScriptResourceCreator.class);
		bindCreationOf(DocumentScriptEnvironment.class).to(DocumentScriptEnvironmentCreator.class);
		bindCreationOf(ModuleScriptEnvironment.class).to(ModuleScriptEnvironmentCreator.class);
		
		
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
