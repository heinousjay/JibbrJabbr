package jj.resource;

import static org.picocontainer.Characteristics.HIDE_IMPL;

import org.picocontainer.MutablePicoContainer;

public class ResourceInitializer {

	public static MutablePicoContainer initialize(MutablePicoContainer container) {
		
		return container
		// it's the new style, from one component springs five
			.addComponent(ResourceCache.class)
			
			
			.addComponent(AssetResourceCreator.class)
			.addComponent(HtmlResourceCreator.class)
			.addComponent(ScriptResourceCreator.class)
			.addComponent(PropertiesResourceCreator.class)
			
			
			// these two depend on each other.  it's very perverse but 
			// unavoidable - the finder needs to be able to set new watches,
			// and the watcher needs to trigger new loads
			// so we hide the impl of the finder. this allows  us to
			// break the dependency cycle with a proxy
			.as(HIDE_IMPL).addComponent(ResourceFinder.class, ResourceFinderImpl.class)
			.addComponent(ResourceWatchService.class)
			
			.addComponent(AssetResourcePreloader.class);
	}
}
