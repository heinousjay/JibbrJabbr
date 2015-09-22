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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStarting;
import jj.ServerStopping;
import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Publisher;
import jj.event.Subscriber;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

@Singleton
@Subscriber
class ResourceWatchServiceLoop extends ServerTask {

	private final ResourceWatchSwitch resourceWatchSwitch;
	
	private final FileWatcher watcher;
	
	private final ResourceCache resourceCache;
	
	private final ResourceLoader resourceLoader;

	private final Publisher publisher;
	
	private final TaskRunner taskRunner;
	
	private final AtomicBoolean run = new AtomicBoolean();
	
	@Inject
	ResourceWatchServiceLoop(
		ResourceWatchSwitch resourceWatchSwitch,
		ResourceCache resourceCache,
		ResourceLoader resourceLoader,
		FileWatcher watcher,
		Publisher publisher,
		TaskRunner taskRunner
	) throws IOException {
		super(ResourceWatchServiceLoop.class.getSimpleName());
		this.resourceWatchSwitch = resourceWatchSwitch;
		this.watcher = watcher;
		this.resourceCache = resourceCache;
		this.resourceLoader = resourceLoader;
		this.publisher = publisher;
		this.taskRunner = taskRunner;
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
		if (run.get() && FileSystemResource.class.isAssignableFrom(event.resourceClass)) {
			watch((FileSystemResource)event.resourceReference.get());
		}
	}

	@Listener
	void on(ServerStarting ignored) {
		if (resourceWatchSwitch.runFileWatcher() && run.compareAndSet(false, watcher.start()) && run.get()) {
			start();
		}
	}

	@Listener
	void on(ServerStopping ignored) {
		if (run.getAndSet(false)) {
			stop();
		}
	}

	@Listener
	void on(ConfigurationLoaded ignored) {
		if (resourceWatchSwitch.runFileWatcher() && !run.get() && !run.getAndSet(watcher.start())) {
			start();
		} else if (!resourceWatchSwitch.runFileWatcher() && run.getAndSet(false)) {
			stop();
		}
	}
	
	private void start() {
		taskRunner.execute(this);
	}
	
	private void stop() {
		watcher.stop();
		interrupt();
	}
	
	@Override
	protected void run() throws Exception {
		while (run.get()) {
			// there's probably a streamy way of doing this but
			// i'm feeling happy enough
			watcher.awaitChangedPaths().forEach((path, action) -> {
				switch (action) {

					case Create:
						publisher.publish(new PathCreation(path));
						break;

					case Delete:
					case Modify:
						resourceCache.findAllByUri(path.toUri()).forEach(resource -> {
							ResourceReloadOrganizer rro = new ResourceReloadOrganizer(resource, action);
							rro.deletions.forEach(this::remove);
							rro.reloads.forEach(this::reload);
						});
						break;

					// the error case!
					case Unknown:
					default:
						throw new AssertionError("unknown file operation on " + path);
				}
			});
		}
	}

	private void remove(final AbstractResource<?> resource) {
		if (resourceCache.remove(resource.cacheKey(), resource)) {
			resource.kill();
		}
	}

	private <T> void reload(final AbstractResource<T> resource) {
		resource.kill();
		resourceLoader.loadResource(resource.type(), resource.base(), resource.name(), resource.creationArg());
	}

	/**
	 * A helper to organize the reloading behavior - we just want to delete things, for the most part,
	 * and only trigger reloads on objects that self-identify as roots
	 * @author jason
	 *
	 */
	private static class ResourceReloadOrganizer {

		final HashSet<AbstractResource<?>> deletions = new HashSet<>();
		final HashSet<AbstractResource<?>> reloads = new HashSet<>();

		ResourceReloadOrganizer(Resource<?> base,  FileWatcher.Action action) {
			placeResource((AbstractResource<?>)base, action == FileWatcher.Action.Delete);
		}

		private void placeResource(AbstractResource<?> resource, boolean delete) {
			// sorry for the javascripty java but the test passes!
			if ((delete || resource.removeOnReload() ? deletions : reloads).add(resource)) {
				// if we haven't seen this one before, traverse it
				for (AbstractResource<?> dependent : resource.dependents()) {
					placeResource(dependent, false);
				}
			}
		}

		@Override
		public String toString() {
			return "Removals: " + deletions + "\n" + "Reloads" + reloads + "\n";
		}
	}
}