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

import jj.configuration.Location;
import jj.configuration.resolution.PathResolver;
import jj.logging.EmergencyLog;

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
	
	private final EmergencyLog logger;
	
	@Inject
	ResourceInstanceCreator(
		final PathResolver pathResolver,
		final Injector parentInjector,
		final EmergencyLog logger
	) {
		this.pathResolver = pathResolver;
		this.parentInjector = parentInjector;
		this.logger = logger;
	}
	
	public <T extends Resource> T createResource(
		final Class<T> type,
		final ResourceKey cacheKey,
		final Location base,
		final String name,
		final Object...args
	) {
		final Path path = pathResolver.resolvePath(base, name);
		
		try {
			
			try {
				
				return parentInjector.createChildInjector(
					new AbstractModule() {
						@Override
						protected void configure() {
							bind(type);
							bind(ResourceKey.class).toInstance(cacheKey);
							bind(Location.class).toInstance(base);
							bind(String.class).toInstance(name);
							if (path != null) {
								bind(Path.class).toInstance(path);
							}
							
							for (Object arg : args) {
								bindInstance(arg.getClass(), arg);
							}
						}
						
						@SuppressWarnings("unchecked")
						private <V> void bindInstance(Class<V> type, Object instance) {
							bind(type).toInstance((V)instance);
						}
					}
				).getInstance(type);
				
			} catch (ProvisionException | CreationException ce) {
				
				Throwable cause = ce.getCause();
				
				if (cause instanceof ResourceNotViableException) throw cause;
				
				throw ce;
			}
			
		} catch (NoSuchResourceException nsre) {
			
			// don't bother logging this, it's just a "not found"
			return null;
			
		} catch (ResourceNotViableException rnve) {
			
			logger.error("", rnve);
			return null;
			
		} catch (Exception e) {
			
			throw new AssertionError("unexpected exception creating a resource", e);
			
		} catch (Throwable t) {
			
			throw (Error)t;
		}
	}
}
