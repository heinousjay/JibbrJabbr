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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.configuration.resolution.PathResolver;
import jj.execution.TaskRunner;

/**
 * walks the application structure from the root, loading all of
 * the directories along the way
 * 
 * @author jason
 *
 */
@Singleton
class DirectoryStructureLoader implements JJServerStartupListener {
	
	private final PathResolver pathResolver;
	private final ResourceFinder resourceFinder;
	private final TaskRunner taskRunner;
	private final CountDownLatch initLatch = new CountDownLatch(1);
	
	@Inject
	DirectoryStructureLoader(final PathResolver pathResolver, final ResourceFinder resourceFinder, final TaskRunner taskRunner) {
		this.pathResolver = pathResolver;
		this.resourceFinder = resourceFinder;
		this.taskRunner = taskRunner;
	}

	@Override
	public void start() throws Exception {
		load(pathResolver.path());
		boolean success = initLatch.await(500, MILLISECONDS);
		assert success : "timed out reading the app directory structure";
	}

	void load(final Path path) {
		taskRunner.execute(new ResourceTask("directory preloader") {
			@Override
			protected void run() throws Exception {
				Files.walkFileTree(path, new FileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						resourceFinder.loadResource(
							DirectoryResource.class,
							pathResolver.base(),
							pathResolver.path().relativize(path.resolve(dir)).toString()
						);
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
				initLatch.countDown();
			}
		});
	}

	@Override
	public Priority startPriority() {
		return Priority.NearHighest;
	}
	
}
