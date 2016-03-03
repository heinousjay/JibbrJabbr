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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.logging.Emergency;
import jj.logging.Warning;

/**
 * Encapsulates the JDK watch service for mockability.  this is also
 * why this class has pretty much zero test coverage.  specifically,
 * on the mac, file watching is implemented with 1 second polling,
 * so we only cover this class in an integration test to keep the
 * unit suite quick. as such, this class should do nothing but translate
 * to the JDK and publish errors
 * 
 * @author jason
 *
 */
@Singleton
class FileWatcher {

	enum Action {
		Unknown, Create, Modify, Delete;

		static Action from(Kind<?> kind) {
			if (kind == ENTRY_CREATE) {
				return Create;
			}
			if (kind == ENTRY_MODIFY) {
				return Modify;
			}
			if (kind == ENTRY_DELETE) {
				return Delete;
			}
			return Unknown;
		}
	}

	private static final boolean MAC_OS_X = "Mac OS X".equals(System.getProperty("os.name"));
	private static final Kind<?>[] FILE_EVENTS = new Kind<?>[] { ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE };
	private static final WatchEvent.Modifier FAST_POLLING_MODIFIER;

	static {
		// if we can find the modifier, we can speed up our polling quite a bit on OS X
		// but this neatly avoids relying on the class
		WatchEvent.Modifier candidate = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Enum<?>> modifierClass =
					(Class<? extends Enum<?>>)Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");

			for (Enum<?> value : modifierClass.getEnumConstants()) {
				if (value.name().equals("HIGH")) {
					candidate = (WatchEvent.Modifier)value;
					break;
				}
			}

		} catch (Exception ignored) {}
		FAST_POLLING_MODIFIER = candidate;
	}

	private final AtomicReference<WatchService> watchServiceRef = new AtomicReference<>();
	private final Publisher publisher;
	
	@Inject
	FileWatcher(Publisher publisher) {
		this.publisher = publisher;
	}
	
	@SuppressWarnings("unchecked")
	private WatchEvent<Path> cast(WatchEvent<?> event) {
		return (WatchEvent<Path>)event;
	}
	
	void watch(Path directory) {
		// this is kind of cheating
		WatchService watchService = watchServiceRef.get();
		assert watchService != null : "asked to watch a file but we aren't even running";
		try {
			if (MAC_OS_X && FAST_POLLING_MODIFIER != null) {
				// this polls on MAC, but the high sensitivity makes it poll a lot, which seems fine
				// for development purposes
				directory.register(watchService, FILE_EVENTS, FAST_POLLING_MODIFIER);
			} else {
				directory.register(watchService, FILE_EVENTS);
			}
		} catch (IOException ioe) {
			publisher.publish(new Emergency("could not watch a directory: " + directory, ioe));
		}
	}

	boolean start() {

		if (watchServiceRef.get() != null) {
			return true; // already running? awesome
		}

		try {
			WatchService watchService = FileSystems.getDefault().newWatchService();
			if (!watchServiceRef.compareAndSet(null, watchService)) {
				publisher.publish(
					new Warning("started a watch service when one was already running! ignored", new Exception("Caller stacktrace"))
				);
				try { watchService.close(); } catch (Exception ignored) {}
			}
			return true;
		} catch (IOException e) {
			publisher.publish(new Emergency("creating a watcher failed", e));
		}

		return false;
	}

	void stop() {
		WatchService watchService = watchServiceRef.getAndSet(null);
		if (watchService != null) {
			try {
				watchService.close();
			} catch (IOException e) {
				publisher.publish(new Warning("closing a watcher threw", e));
			}
		}
	}

	Map<Path, Action> awaitChangedPaths() throws InterruptedException {
		assert watchServiceRef.get() != null : "awaiting changes but never started!";
		Map<Path, Action> result = new HashMap<>();
		while (result.isEmpty()) {
			WatchKey watchKey = watchServiceRef.get().take();
			if (watchKey.isValid()) {
				final Path directory = (Path)watchKey.watchable();
				for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
					final Kind<?> kind = watchEvent.kind();
					
					if (kind == OVERFLOW) {
						// not sure what to do about this. probably need to
						// flush the whole cache in this scenario and reload
						// the directories from the base
						publisher.publish(new Warning("event overflow in the file watcher.  changes were missed!"));
						
					} else {

						final WatchEvent<Path> event = cast(watchEvent);
						final Path context = event.context();
						final Path path = directory.resolve(context);
						result.put(path, Action.from(kind));
					}
					
				}

				// gotta clean up after ourselves?
				// i'm not totally clear on if this is necessary but it seems to work
				// cargo cult cancel!
				if (!Files.exists(directory)) {
					watchKey.cancel();
				}
			}
			watchKey.reset();
		}
		return result;
	}

}
