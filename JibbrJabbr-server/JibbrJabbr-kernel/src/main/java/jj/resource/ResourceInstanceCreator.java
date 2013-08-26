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

import org.slf4j.Logger;

import jj.logging.EmergencyLogger;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Injector;

/**
 * By my convention, this would be the resource creator... which
 * I do not want it to be
 * 
 * @author jason
 *
 */
@Singleton
class ResourceInstanceCreator {

	private final Injector parentInjector;
	
	private final Logger logger;
	
	private final class ResourceInstanceModule extends AbstractModule {

		private final Class<? extends Resource> type;
		private final ResourceCacheKey cacheKey;
		private final String baseName;
		private final Path path;
		private final Object[] args;
		
		ResourceInstanceModule(
			final Class<? extends Resource> type,
			final ResourceCacheKey cacheKey,
			final String baseName,
			final Path path,
			final Object[] args
		) {
			this.type = type;
			this.cacheKey = cacheKey;
			this.baseName = baseName;
			this.path = path;
			this.args = args;
		}
		
		@Override
		protected void configure() {
			// bind up all the resource classes
			bind(type);
			bind(ResourceCacheKey.class).toInstance(cacheKey);
			bind(String.class).toInstance(baseName);
			bind(Path.class).toInstance(path);
			
			for (Object arg : args) {
				bindInstance(arg.getClass(), arg);
			}
		}
		
		@SuppressWarnings("unchecked")
		private <T> void bindInstance(Class<T> type, Object instance) {
			bind(type).toInstance((T)instance);
		}
		
	}
	
	@Inject
	ResourceInstanceCreator(
		final Injector parentInjector,
		final @EmergencyLogger Logger logger
	) {
		this.parentInjector = parentInjector;
		this.logger = logger;
	}
	
	<T extends Resource> T createResource(
		final Class<T> type,
		final ResourceCacheKey cacheKey,
		final String baseName,
		final Path path,
		final Object...args
	) {
		try {
			
			try {
				
				return parentInjector.createChildInjector(
					new ResourceInstanceModule(type, cacheKey, baseName, path, args)
				).getInstance(type);
				
			} catch (CreationException ce) {
				
				throw ce.getCause();
			}
			
		} catch (NoSuchResourceException nsre) {
			
			// don't bother logging
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
