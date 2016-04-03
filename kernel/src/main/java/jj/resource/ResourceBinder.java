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

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * @author jason
 *
 */
class ResourceBinder {
	
	private final Map<Class<? extends Resource<?>>, ResourceBindingProcessor<? extends Resource<?>>> bindingProcessors = new HashMap<>();

	private final MapBinder<Class<? extends AbstractResource<?>>, SimpleResourceCreator<? extends AbstractResource<?>, ?>> resourceCreatorBinder;
	
	ResourceBinder(final Binder binder) {
		resourceCreatorBinder = MapBinder.newMapBinder(
			binder,
			new TypeLiteral<Class<? extends AbstractResource<?>>>() {},
			new TypeLiteral<SimpleResourceCreator<? extends AbstractResource<?>, ?>>() {}
		);
	}
	
	<A, T extends Resource<A>> ResourceBinder addResourceBindingProcessor(Class<T> resourceClass, ResourceBindingProcessor<?> bindingProcessor) {
		bindingProcessors.put(resourceClass, bindingProcessor);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractResource<A>, A, U extends SimpleResourceCreator<T, A>> LinkedBindingBuilder<U> of(Class<T> key) {

		bindingProcessors.keySet().stream()
			.filter(
				resourceInterface ->
					resourceInterface.isAssignableFrom(key)
			).forEach(resourceInterface -> {
				ResourceBindingProcessor<T> processor = (ResourceBindingProcessor<T>) bindingProcessors.get(resourceInterface);
				processor.process(key);
			}
		);
		
		return (LinkedBindingBuilder<U>)resourceCreatorBinder.addBinding(key);
	}
}
