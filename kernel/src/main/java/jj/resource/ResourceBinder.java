/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.resource;

import jj.http.server.SimpleRouteProcessor;
import jj.http.uri.RouteProcessor;
import jj.util.StringUtils;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * @author jason
 *
 */
public class ResourceBinder {

	private final MapBinder<Class<? extends AbstractResource>, SimpleResourceCreator<? extends AbstractResource>> resourceCreatorBinder;
	
	private final MapBinder<String, Class<? extends ServableResource>> servableResourceBinder;
	
	private final MapBinder<Class<? extends ServableResource>, RouteProcessor> resourceServerBinder;
	
	public ResourceBinder(final Binder binder) {
		resourceCreatorBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends AbstractResource>>() {},
			new TypeLiteral<SimpleResourceCreator<? extends AbstractResource>>() {}
		);
		
		servableResourceBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<String>() {},
			new TypeLiteral<Class<? extends ServableResource>>() {}
		);
		
		resourceServerBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends ServableResource>>() {},
			new TypeLiteral<RouteProcessor>() {}
		);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractResource, U extends SimpleResourceCreator<T>> LinkedBindingBuilder<U> of(Class<T> key) {
		
		if (ServableResource.class.isAssignableFrom(key)) {
			
			String name = null;
			Class<? extends RouteProcessor> resourceServerClass = SimpleRouteProcessor.class;
			
			ServableConfiguration config = key.getAnnotation(ServableConfiguration.class);
			if (config != null) {
				name = config.name();
				resourceServerClass = config.processor();
			}
			
			if (StringUtils.isEmpty(name)) {
				// figure it out! this is close but not bulletproof
				name = key.getSimpleName();
				int end = name.lastIndexOf("Resource");
				if (end > 1) {
					name = name.substring(0, end);
				}
				name = name.substring(0, 1).toLowerCase() + name.substring(1);
			}
			
			servableResourceBinder.addBinding(name).toInstance((Class<? extends ServableResource>)key);
			resourceServerBinder.addBinding((Class<? extends ServableResource>)key).to(resourceServerClass);
		}
		
		return (LinkedBindingBuilder<U>)resourceCreatorBinder.addBinding(key);
	}
}
