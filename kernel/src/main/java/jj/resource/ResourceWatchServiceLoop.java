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

	
	private void remove(final AbstractResource resource) {
		// publish this event!
		if (resourceCache.remove(resource.cacheKey(), resource)) {
			resource.kill();
		}
	}
	
	private void reload(final AbstractResource resource) {
		resource.kill();
		taskRunner.execute(
			new ResourceTask(getClass().getSimpleName() + " reloader for " + resource.cacheKey()) {


				@Override
				public void run() {
					resourceFinder.loadResource(
						resource.getClass(),
						resource.cacheKey().base(),
						resource.name(),
						resource.creationArgs()
					);
				}
			}
		);
	}
	
	void watch(FileResource resource) throws IOException {
		final Path directory = resource.path().getParent();
		if (directory.getFileSystem() == FileSystems.getDefault()) {
			watcher.watch(directory);
		}
	}
	
	@Override
	protected void run() throws Exception {
		while (true) {
			Map<URI, Boolean> uris = watcher.awaitChangedUris();
			for (URI uri : uris.keySet()) {
				for (final Resource resource : resourceCache.findAllByUri(uri)) {
					ResourceReloadOrganizer rro = 
						new ResourceReloadOrganizer((AbstractResource)resource, uris.get(uri));
					for (AbstractResource ar : rro.deletions) {
						remove(ar);
					}
					for (AbstractResource ar : rro.reloads) {
						reload(ar);
					}
				}
			}
		}
	}
}