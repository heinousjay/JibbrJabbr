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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.execution.ServerTask;
import jj.execution.TaskRunner;

@Singleton
class ResourceWatchServiceLoop extends ServerTask {
	
	private final ResourceWatcher watcher;
	
	private final ResourceCache resourceCache;
	
	private final ResourceFinder resourceFinder;
	
	private final TaskRunner taskRunner;
	
	private volatile boolean run = true;
	
	@Inject
	ResourceWatchServiceLoop(
		final ResourceCache resourceCache,
		final ResourceFinder resourceFinder,
		final ResourceWatcher watcher,
		final TaskRunner taskRunner
	) throws IOException {
		super(ResourceWatchServiceLoop.class.getSimpleName());
		this.watcher = watcher;
		this.resourceCache = resourceCache;
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
	}

	
	private void remove(final AbstractResource<?> resource) {
		if (resourceCache.remove(resource.cacheKey(), resource)) {
			resource.kill();
		}
	}
	
	private  void reload(final AbstractResource<?> resource) {
		resource.kill();
		taskRunner.execute(
			new ResourceTask(getClass().getSimpleName() + " reloader for " + resource.cacheKey()) {


				@SuppressWarnings("unchecked") // because argument type checking gets effy
				@Override
				public void run() {
					resourceFinder.loadResource(
						resource.getClass(),
						resource.base(),
						resource.name(),
						resource.creationArg()
					);
				}
			}
		);
	}
	
	void watch(FileSystemResource resource) throws IOException {
		Path path = resource.path();
		
		if (path.getFileSystem() == FileSystems.getDefault()) {
			watcher.watch(resource.isDirectory() ? path : path.getParent());
		}
	}
	
	void start() {
		run = true;
		taskRunner.execute(this);
	}
	
	void stop() {
		run = false;
		interrupt();
	}
	
	@Override
	protected void run() throws Exception {
		while (run) {
			Map<URI, Boolean> uris = watcher.awaitChangedUris();
			for (URI uri : uris.keySet()) {
				for (final Resource<?> resource : resourceCache.findAllByUri(uri)) {
					ResourceReloadOrganizer rro = 
						new ResourceReloadOrganizer((AbstractResource<?>)resource, uris.get(uri));
					for (AbstractResource<?> ar : rro.deletions) {
						remove(ar);
					}
					for (AbstractResource<?> ar : rro.reloads) {
						reload(ar);
					}
				}
			}
		}
	}
}