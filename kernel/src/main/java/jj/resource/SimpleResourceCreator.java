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
 * {@link #create(Location, String, A)}, otherwise just extend
 * this class and bind it.
 * 
 * <p>
 * If all you need to do is validate the arguments are correct, override
 * {@link #arguments(Location, String, A)} and return true if everything is
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
		protected final ResourceIdentifierMaker resourceIdentifierMaker;
		
		@Inject
		protected Dependencies(PathResolver pathResolver, ResourceInstanceCreator creator, ResourceIdentifierMaker resourceIdentifierMaker) {
			this.pathResolver = pathResolver;
			this.creator = creator;
			this.resourceIdentifierMaker = resourceIdentifierMaker;
		}
	}
	protected final PathResolver pathResolver;
	protected final ResourceInstanceCreator creator;
	protected final ResourceIdentifierMaker resourceIdentifierMaker;
	private final Class<T> type;
	private final Class<A> argType;
	
	@SuppressWarnings("unchecked")
	@Inject
	protected SimpleResourceCreator(final Dependencies dependencies) {
		pathResolver = dependencies.pathResolver;
		creator = dependencies.creator;
		resourceIdentifierMaker = dependencies.resourceIdentifierMaker;
		type = (Class<T>)GenericUtils.extractTypeParameterAsClass(getClass(), SimpleResourceCreator.class, "T");
		argType = (Class<A>)GenericUtils.extractTypeParameterAsClass(getClass(), SimpleResourceCreator.class, "A");
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
	
	protected boolean arguments(Location base, String name, A argument) {
		return true;
	}

	@Override
	public T create(Location base, String name, A argument) throws IOException {
		assert arguments(base, name, argument) : getClass().getName();
		return creator.createResource(resourceIdentifierMaker.make(type(), base, name, argument));
	}
}
