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

import jj.execution.IOTask;
import jj.execution.TaskRunner;
import jj.execution.JJTask;

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
	
	public void loadResource(final Class<? extends Resource> resourceClass, final String name,  final Object... arguments) {
		loadResource(null, resourceClass, name, arguments);
	}
	
	public void loadResource(final JJTask next, final Class<? extends Resource> resourceClass, final String name,  final Object... arguments) {
		taskRunner.execute(new ResourceLoaderTask(resourceClass, name, arguments).thenRun(next));
	}
	
	private final class ResourceLoaderTask extends IOTask {
		
		private final Class<? extends Resource> resourceClass;
		private final String name;
		private final Object[] arguments;
		
		private JJTask next;

		/**
		 * @param name
		 */
		public ResourceLoaderTask(final Class<? extends Resource> resourceClass, final String name, final Object... arguments) {
			super("Resource loader [" + resourceClass.getSimpleName() + " at " + name);
			this.resourceClass = resourceClass;
			this.name = name;
			this.arguments = arguments;
			
		}
		
		// i almost feel like this should be in the base class of all tasks
		// or possibly the return from the execute method, if i can get rid of the future
		public ResourceLoaderTask thenRun(JJTask next) {
			this.next = next;
			return this;
		}

		@Override
		protected void run() throws Exception {
			resourceFinder.loadResource(resourceClass, name, arguments);
			if (next != null) {
				taskRunner.execute(next);
			}
		}
	}
}
