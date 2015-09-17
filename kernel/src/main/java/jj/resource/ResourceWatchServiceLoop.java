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

import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

@Singleton
@Subscriber
class ResourceWatchServiceLoop extends ServerTask {

	private final ResourceWatchSwitch resourceWatchSwitch;
	
	private final ResourceWatcher watcher;
	
	private final ResourceCache resourceCache;
	
	private final ResourceFinder resourceFinder;
	
	private final TaskRunner taskRunner;
	
	private volatile boolean run = true;
	
	@Inject
	ResourceWatchServiceLoop(
		ResourceWatchSwitch resourceWatchSwitch,
		final ResourceCache resourceCache,
		final ResourceFinder resourceFinder,
		final ResourceWatcher watcher,
		final TaskRunner taskRunner
	) throws IOException {
		super(ResourceWatchServiceLoop.class.getSimpleName());
		this.resourceWatchSwitch = resourceWatchSwitch;
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
	
	private void reload(final AbstractResource<?> resource) {
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
	
	private void watch(FileSystemResource resource) {
		if (resource != null) {
			Path path = resource.path();

			if (path.getFileSystem() == FileSystems.getDefault()) {
				watcher.watch(resource.isDirectory() ? path : path.getParent());
			}
		}
	}

	@Listener
	void on(ResourceLoaded event) {
		if (resourceWatchSwitch.runFileWatcher() && FileSystemResource.class.isAssignableFrom(event.resourceClass)) {
			watch((FileSystemResource)event.resourceReference.get());
		}
	}

	@Listener
	void on(ConfigurationLoaded event) {
		if (resourceWatchSwitch.runFileWatcher()) {
			start();
		} else {
			stop();
		}
	}
	
	private void start() {
		run = true;
		taskRunner.execute(this);
	}
	
	private void stop() {
		run = false;
		interrupt();
	}
	
	@Override
	protected void run() throws Exception {
		while (run) {
			// there's probably a streamy way of doing this but
			// i'm feeling happy enough
			Map<URI, Boolean> uris = watcher.awaitChangedUris();
			uris.keySet().forEach(uri -> {
				resourceCache.findAllByUri(uri).forEach(resource -> {
						ResourceReloadOrganizer rro =
							new ResourceReloadOrganizer((AbstractResource<?>) resource, uris.get(uri));
						rro.deletions.forEach(this::remove);
						rro.reloads.forEach(this::reload);
					}
				);
			});
		}
	}
}