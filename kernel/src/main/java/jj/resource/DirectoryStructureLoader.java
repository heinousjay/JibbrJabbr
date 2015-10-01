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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStarting;
import jj.ServerStarting.Priority;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.TaskRunner;

/**
 * <p>
 * Service that loads directory structures as {@link DirectoryResource}s
 *
 * <p>
 * Responsible for the initial load of directory structures during system startup,
 * and also listens for new directory creation
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class DirectoryStructureLoader {
	
	private final PathResolver pathResolver;
	private final ResourceFinder resourceFinder;
	private final TaskRunner taskRunner;
	
	@Inject
	DirectoryStructureLoader(PathResolver pathResolver, ResourceFinder resourceFinder, TaskRunner taskRunner) {
		this.pathResolver = pathResolver;
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
	}

	@Listener
	void on(ServerStarting event) {
		for (Location location : pathResolver.watchedLocations()) {
			event.registerStartupTask(Priority.NearHighest, new LoaderTask(pathResolver.resolvePath(location)));
		}
	}

	@Listener
	void on(DirectoryCreation creation) {
		load(creation.path);
	}
	
	private void load(Path path) {
		Location base = pathResolver.resolveLocation(path);
		assert base != null && base.parentInDirectory() : "asked to load a directory structure for a bad path: " + path;
		taskRunner.execute(new LoaderTask(path));
	}
	
	private class LoaderTask extends ResourceTask {
		
		private final Path path;
		
		LoaderTask(Path path) {
			super("loading directory structure rooted at " + path);
			this.path = path;
		}
		
		@Override
		protected void run() throws Exception {
			// we check if the path is a directory here in ResourceThread context,
			// so that the external methods can be invoked without incurring I/O costs
			assert Files.isDirectory(path) : "asked to load a file " + path;
			Files.walkFileTree(path, new FileVisitor<Path>() {

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Location location = pathResolver.resolveLocation(dir);
					if (location.parentInDirectory()) {
						resourceFinder.loadResource(
								DirectoryResource.class,
								location,
								pathResolver.resolvePath(location).relativize(path.resolve(dir)).toString()
						);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}
}
