package jj.resource;

import jj.HasBinder;
import jj.http.server.ServableResource;
import jj.http.server.ServableResourceBindingProcessor;
import jj.http.server.websocket.WebSocketConnectionHost;
import jj.http.server.websocket.WebSocketConnectionHostBindingProcessor;

/**
 * Mixin to register resource types in the system
 *
 * Created by jasonmiller on 4/3/16.
 */
public interface BindsResourceCreation extends HasBinder {

	interface Using<T extends AbstractResource<A>, A>  {
		void using(Class<? extends SimpleResourceCreator<T, A>> creatorClass);
	}

	default <T extends AbstractResource<A>, A> Using<T, A> createResource(Class<T> resourceClass) {
		return creatorClass ->
			new ResourceBinder(binder())
				.addResourceBindingProcessor(ServableResource.class, new ServableResourceBindingProcessor(binder()))
				.addResourceBindingProcessor(WebSocketConnectionHost.class, new WebSocketConnectionHostBindingProcessor(binder()))
				.of(resourceClass)
				.to(creatorClass);
	}
}
