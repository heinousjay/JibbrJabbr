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

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * Encapsulates the JDK watch service for mockability.  this is also
 * why this class has pretty much zero test coverage.  specifically,
 * on the mac, file watching is implemented with 10 second polling,
 * so we only cover this class in an integration test to keep the
 * unit suite quick
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class ResourceWatcher {

	private final WatchService watcher;
	private final DirectoryStructureLoader directoryStructureLoader;
	
	@Inject
	ResourceWatcher(final DirectoryStructureLoader directoryStructureLoader) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		this.directoryStructureLoader = directoryStructureLoader;
	}
	
	@SuppressWarnings("unchecked")
	private WatchEvent<Path> cast(WatchEvent<?> event) {
		return (WatchEvent<Path>)event;
	}
	
	void watch(Path directory) throws IOException {
		directory.register(watcher, ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE);
	}
	
	@Listener
	public void stop(ServerStopping event) {
		try {
			watcher.close();
		} catch (IOException e) {}
	};

	Map<URI, Boolean> awaitChangedUris() throws InterruptedException {
		Map<URI, Boolean> result = new HashMap<>();
		while (result.isEmpty()) {
			WatchKey watchKey = watcher.take();
			if (watchKey.isValid()) {
				final Path directory = (Path)watchKey.watchable();
				for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
					final Kind<?> kind = watchEvent.kind();
					
					if (kind == OVERFLOW) {
						// not sure what to do about this. probably need to
						// flush the whole cache in this scenario and reload
						// the directories from the base
						System.err.println(getClass().getSimpleName() + " OVERFLOW - not even sure what this means!");
						continue; // for now.
						
					} else {

						final WatchEvent<Path> event = cast(watchEvent);
						final Path context = event.context();
						final Path path = directory.resolve(context);
						
						// if something was changed, get our reload on
						if (kind == ENTRY_DELETE || kind == ENTRY_MODIFY) {
							result.put(path.toUri(), kind == ENTRY_DELETE);
						}
						
						// if this is a new directory, then walk it
						else if (kind == ENTRY_CREATE && Files.isDirectory(path)) {
							directoryStructureLoader.load(path);
						}
					}
					
				}
				if (!Files.exists(directory)) {
					watchKey.cancel();
				}
			}
			watchKey.reset();
		}
		return result;
	}

}
