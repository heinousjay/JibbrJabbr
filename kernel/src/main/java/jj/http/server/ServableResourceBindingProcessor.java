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
import com.google.inject.multibindings.Multibinder;

import jj.resource.Resource;
import jj.resource.ResourceBindingProcessor;
import jj.util.StringUtils;

/**
 * @author jason
 *
 */
public class ServableResourceBindingProcessor implements ResourceBindingProcessor<ServableResource> {
	
	private final MapBinder<String, Class<? extends ServableResource>> servableResourceBinder;
	
	private final MapBinder<String, RouteProcessor> routeProcessorBinder;
	
	private final Multibinder<RouteContributor> routeContributorBinder;
	
	public ServableResourceBindingProcessor(Binder binder) {
		servableResourceBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<String>() {},
			new TypeLiteral<Class<? extends ServableResource>>() {}
		);
		
		routeProcessorBinder = MapBinder.newMapBinder(binder, String.class, RouteProcessor.class);
		
		routeContributorBinder = Multibinder.newSetBinder(binder, RouteContributor.class);
	}

	@Override
	public void process(Class<? extends Resource> binding) {
		
		assert ServableResource.class.isAssignableFrom(binding);
		
		@SuppressWarnings("unchecked")
		Class<? extends ServableResource> resourceClassBinding = (Class<? extends ServableResource>)binding;
		
		String name = null;
		Class<? extends RouteProcessor> routeProcessorClass = SimpleRouteProcessor.class;
		Class<? extends RouteContributor> routeContributorClass = EmptyRouteContributor.class;
		
		ServableResourceConfiguration config = resourceClassBinding.getAnnotation(ServableResourceConfiguration.class);
		if (config != null) {
			name = config.name();
			routeProcessorClass = config.processor();
			routeContributorClass = config.routeContributor();
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
		routeProcessorBinder.addBinding(name).to(routeProcessorClass);
		
		if (routeContributorClass != EmptyRouteContributor.class) {
			routeContributorBinder.addBinding().to(routeContributorClass);
		}
	}

}
