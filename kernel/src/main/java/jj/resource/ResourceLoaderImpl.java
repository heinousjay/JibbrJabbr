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
 * if you need to load a resource, here's a simple way to do it
 * 
 * @author jason
 *
 */
@Singleton
class ResourceLoaderImpl implements ResourceLoader {
	
	private final TaskRunner taskRunner;
	private final ResourceFinder resourceFinder;
	
	@Inject
	ResourceLoaderImpl(final TaskRunner taskRunner, final ResourceFinder resourceFinder) {
		this.taskRunner = taskRunner;
		this.resourceFinder = resourceFinder;
	}
	
	@Override
	public <T extends Resource<Void>> T findResource(Class<T> resourceClass, Location base, String name) {
		return findResource(resourceClass, base, name, null);
	}
	
	@Override
	public <A, T extends Resource<A>> T findResource(Class<T> resourceClass, Location base, String name, A argument) {
		return resourceFinder.findResource(resourceClass, base, name, argument);
	}
	
	@Override
	public <T extends Resource<Void>> Promise loadResource(Class<T> resourceClass, Location base, String name) {
		return loadResource(resourceClass, base, name, null);
	}
	
	@Override
	public <A, T extends Resource<A>> Promise loadResource(final Class<T> resourceClass, final Location base, final String name,  final A argument) {
		return taskRunner.execute(new ResourceLoaderTask<>(resourceClass, base, name, argument));
	}
	
	private final class ResourceLoaderTask<A, T extends Resource<A>> extends ResourceTask {
		
		private final Class<T> resourceClass;
		private final Location base;
		private final String name;
		private final A argument;

		private ResourceLoaderTask(final Class<T> resourceClass, final Location base, final String name, final A argument) {
			super("Resource loader [" + resourceClass.getSimpleName() + "@" + base + "/" + name + "]");
			this.resourceClass = resourceClass;
			this.base = base;
			this.name = name;
			this.argument = argument;
			
		}

		@Override
		protected void run() throws Exception {
			resourceFinder.loadResource(resourceClass, base, name, argument);
		}
	}
}
