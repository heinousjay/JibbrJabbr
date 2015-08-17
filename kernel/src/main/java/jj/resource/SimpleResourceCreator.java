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

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.util.GenericUtils;

/**
 * <p>
 * Extend this to declare a {@link ResourceCreator}. It must be extended
 * to allow the construction system to use the generic type parameter.
 * 
 * <p>
 * If the resource being created has special creation needs, override
 * {@link #create(Location, String, Object...)}, otherwise just extend
 * this class and bind it.
 * 
 * <p>
 * If all you need to do is validate the arguments are correct, override
 * {@link #arguments(Location, String)} and return true if everything is
 * fine, false if not.
 * 
 * @author jason
 *
 */
@Singleton
public class SimpleResourceCreator<A, T extends AbstractResource<A>> implements ResourceCreator<A, T> {
	
	@Singleton
	public static class Dependencies {
		protected final PathResolver pathResolver;
		protected final ResourceInstanceCreator creator;
		
		@Inject
		protected Dependencies(final PathResolver pathResolver, final ResourceInstanceCreator creator) {
			this.pathResolver = pathResolver;
			this.creator = creator;
		}
	}
	protected final PathResolver pathResolver;
	protected final ResourceInstanceCreator creator;
	private final Class<T> type;
	private final Class<A> argType;
	
	@SuppressWarnings("unchecked")
	@Inject
	protected SimpleResourceCreator(final Dependencies dependencies) {
		pathResolver = dependencies.pathResolver;
		creator = dependencies.creator;
		type = (Class<T>)GenericUtils.extractGenericParameter(getClass(), 1);
		argType = (Class<A>)GenericUtils.extractGenericParameter(getClass());
	}

	protected URI uri(final Location base, final String name, final A argument) {
		return pathResolver.resolvePath(base, name).toUri();
	}
	
	public Class<A> argType() {
		return argType;
	}
	
	@Override
	public Class<T> type() {
		return type;
	}
	
	@Override
	public ResourceKey resourceKey(final Location base, final String name, final A argument) {
		return new ResourceKey(type, uri(base, name, argument));
	}
	
	protected boolean arguments(Location base, String name, A argument) {
		return true;
	}

	@Override
	public T create(Location base, String name, A argument) throws IOException {
		assert arguments(base, name, argument);
		return creator.createResource(type, resourceKey(base, name, argument), base, name, argument);
	}
}
