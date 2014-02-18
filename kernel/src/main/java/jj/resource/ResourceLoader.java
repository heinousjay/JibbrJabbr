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

import jj.configuration.AppLocation;
import jj.execution.Promise;
import jj.execution.TaskRunner;

/**
 * if you need to load a resource, here's a simple way to do it
 * 
 * @author jason
 *
 */
@Singleton
public class ResourceLoader {
	
	private final TaskRunner taskRunner;
	private final ResourceFinder resourceFinder;
	
	@Inject
	ResourceLoader(final TaskRunner taskRunner, final ResourceFinder resourceFinder) {
		this.taskRunner = taskRunner;
		this.resourceFinder = resourceFinder;
	}
	
	public Promise loadResource(final Class<? extends Resource> resourceClass, final AppLocation base, final String name,  final Object...arguments) {
		return taskRunner.execute(new ResourceLoaderTask(resourceClass, base, name, arguments));
	}
	
	private final class ResourceLoaderTask extends ResourceTask {
		
		private final Class<? extends Resource> resourceClass;
		private final AppLocation base;
		private final String name;
		private final Object[] arguments;

		/**
		 * @param name
		 */
		public ResourceLoaderTask(final Class<? extends Resource> resourceClass, final AppLocation base, final String name, final Object...arguments) {
			super("Resource loader [" + resourceClass.getSimpleName() + " at " + name);
			this.resourceClass = resourceClass;
			this.base = base;
			this.name = name;
			this.arguments = arguments;
			
		}

		@Override
		protected void run() throws Exception {
			resourceFinder.loadResource(resourceClass, base, name, arguments);
		}
	}
}
