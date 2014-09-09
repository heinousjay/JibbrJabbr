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
package jj.http.server;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

import jj.http.server.uri.RouteProcessor;
import jj.resource.Resource;
import jj.resource.ResourceBindingProcessor;
import jj.util.StringUtils;

/**
 * @author jason
 *
 */
public class ServableResourceBindingProcessor implements ResourceBindingProcessor<ServableResource> {
	
	private final MapBinder<String, Class<? extends ServableResource>> servableResourceBinder;
	
	private final MapBinder<Class<? extends ServableResource>, RouteProcessor> resourceServerBinder;
	
	public ServableResourceBindingProcessor(Binder binder) {
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

	@Override
	public void process(Class<? extends Resource> binding) {
		
		assert ServableResource.class.isAssignableFrom(binding);
		
		@SuppressWarnings("unchecked")
		Class<? extends ServableResource> resourceClassBinding = (Class<? extends ServableResource>)binding;
		
		String name = null;
		Class<? extends RouteProcessor> resourceServerClass = SimpleRouteProcessor.class;
		
		ServableConfiguration config = resourceClassBinding.getAnnotation(ServableConfiguration.class);
		if (config != null) {
			name = config.name();
			resourceServerClass = config.processor();
		}
		
		if (StringUtils.isEmpty(name)) {
			// figure it out! this is close but not bulletproof
			name = resourceClassBinding.getSimpleName();
			int end = name.lastIndexOf("Resource");
			if (end > 1) {
				name = name.substring(0, end);
			}
			name = name.substring(0, 1).toLowerCase() + name.substring(1);
		}
		
		servableResourceBinder.addBinding(name).toInstance(resourceClassBinding);
		resourceServerBinder.addBinding(resourceClassBinding).to(resourceServerClass);
	}

}
