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

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.Promise;
import jj.execution.TaskRunner;

/**
 * @author jason
 *
 */
@Singleton
class ResourceLoaderImpl implements ResourceLoader {
	
	private final TaskRunner taskRunner;
	private final ResourceFinder resourceFinder;
	private final ResourceIdentifierMaker maker;
	
	@Inject
	ResourceLoaderImpl(TaskRunner taskRunner, ResourceFinder resourceFinder, ResourceIdentifierMaker maker) {
		this.taskRunner = taskRunner;
		this.resourceFinder = resourceFinder;
		this.maker = maker;
	}

	@Override
	public <T extends Resource<A>, A> T findResource(ResourceIdentifier<T, A> identifier) {
		return resourceFinder.findResource(identifier);
	}

	@Override
	public <T extends Resource<Void>> T findResource(Class<T> resourceClass, Location base, String name) {
		return findResource(maker.make(resourceClass, base, name));
	}
	
	@Override
	public <T extends Resource<A>, A> T findResource(Class<T> resourceClass, Location base, String name, A argument) {
		return findResource(maker.make(resourceClass, base, name, argument));
	}

	@Override
	public <T extends Resource<A>, A> Promise loadResource(ResourceIdentifier<T, A> identifier) {
		return taskRunner.execute(new ResourceLoaderTask<>(identifier));
	}
	
	@Override
	public <T extends Resource<Void>> Promise loadResource(Class<T> resourceClass, Location base, String name) {
		return loadResource(maker.make(resourceClass, base, name));
	}
	
	@Override
	public <T extends Resource<A>, A> Promise loadResource(final Class<T> resourceClass, final Location base, final String name,  final A argument) {
		return loadResource(maker.make(resourceClass, base, name, argument));
	}
	
	private final class ResourceLoaderTask<T extends Resource<A>, A> extends ResourceTask {
		
		private final ResourceIdentifier<T, A> identifier;

		private ResourceLoaderTask(ResourceIdentifier<T, A> identifier) {
			super("Resource loader [" + identifier + "]");
			this.identifier = identifier;
			
		}

		@Override
		protected void run() throws Exception {
			resourceFinder.loadResource(identifier);
		}
	}
}
