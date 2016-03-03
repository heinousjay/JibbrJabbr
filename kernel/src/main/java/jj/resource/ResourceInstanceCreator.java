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

import com.google.inject.*;
import jj.event.Publisher;

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
	
	public <T extends Resource<A>, A> T createResource(ResourceIdentifier<T, A> identifier) {
		// ideally! base.resolve(name);
		// but that requires tricks
		final Path path = pathResolver.resolvePath(identifier.base, identifier.name);
		
		try {
			
			try {
				
				Injector inj = parentInjector.createChildInjector(
					new AbstractModule() {
						@Override
						protected void configure() {
							if (path != null) {
								bind(Path.class).toInstance(path);
							}

							bind(identifier.resourceClass);
							bind(new TypeLiteral<ResourceIdentifier<?, ?>>() {}).toInstance(identifier);

							// bind this specifically to get a convenient param in the constructor
							if (identifier.argument != null) {
								bindInstance(identifier.argument.getClass(), identifier.argument);
							}
						}
						
						@SuppressWarnings("unchecked")
						private <V> void bindInstance(Class<V> type, Object instance) {
							bind(type).toInstance((V)instance);
						}
					}
				);
				
				return inj.getInstance(identifier.resourceClass);
				
			} catch (ProvisionException | CreationException ce) {
				
				Throwable cause = ce.getCause();
				
				if (cause instanceof ResourceNotViableException) throw (ResourceNotViableException)cause;
				
				throw ce;
			}
			
		} catch (NoSuchResourceException nsre) {
			// don't bother logging this, it's just a "not found" and will be handled in the ResourceLoaderImpl
		} catch (Exception e) {
			publisher.publish(new ResourceError(identifier, e));
		}
		
		return null;
	}
}
