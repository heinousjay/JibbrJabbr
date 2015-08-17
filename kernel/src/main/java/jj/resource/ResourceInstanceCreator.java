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

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

/**
 * Handles instantiating Resource instances with full injection and common error handling.
 * 
 * @author jason
 *
 */
@Singleton
public class ResourceInstanceCreator {
	
	private final PathResolver pathResolver;

	private final Injector parentInjector;
	
	private final Publisher publisher;
	
	@Inject
	ResourceInstanceCreator(
		final PathResolver pathResolver,
		final Injector parentInjector,
		final Publisher publisher
	) {
		this.pathResolver = pathResolver;
		this.parentInjector = parentInjector;
		this.publisher = publisher;
	}
	
	public <A, T extends Resource<A>> T createResource(
		final Class<T> resourceClass,
		final ResourceKey resourceKey,
		final Location base,
		final String name,
		final A argument
	) {
		// ideally! base.resolve(name);
		// but that requires tricks
		final Path path = pathResolver.resolvePath(base, name);
		
		try {
			
			try {
				
				Injector inj = parentInjector.createChildInjector(
					new AbstractModule() {
						@Override
						protected void configure() {
							bind(resourceClass);
							bind(ResourceKey.class).toInstance(resourceKey);
							bind(Location.class).toInstance(base);
							bind(String.class).annotatedWith(ResourceName.class).toInstance(name);
							if (path != null) {
								bind(Path.class).toInstance(path);
							}
							
							if (argument != null) {
								bindInstance(argument.getClass(), argument);
							}
						}
						
						@SuppressWarnings("unchecked")
						private <V> void bindInstance(Class<V> type, Object instance) {
							bind(type).toInstance((V)instance);
						}
					}
				);
				
				return inj.getInstance(resourceClass);
				
			} catch (ProvisionException | CreationException ce) {
				
				Throwable cause = ce.getCause();
				
				if (cause instanceof ResourceNotViableException) throw (ResourceNotViableException)cause;
				
				throw ce;
			}
			
		} catch (NoSuchResourceException nsre) {
			// don't bother logging this, it's just a "not found" and will be handled in the ResourceLoaderImpl
		} catch (Exception e) {
			publisher.publish(new ResourceError(resourceClass, base, name, argument, e));
		}
		
		return null;
	}
}
