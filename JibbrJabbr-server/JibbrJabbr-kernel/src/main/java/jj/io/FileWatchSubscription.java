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
package jj.io;


import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

import jj.api.NonBlocking;

import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * API to the FileWatchService.
 * </p>
 * 
 * <p>
 * To watch a particular path for changes:<pre>
 * Path toWatch = ...;
 * new FileWatchSubscription(toWatch) {
 *     protected void fileChanged(Path path, Kind<Path> kind) {
 *         // do something interesting now that you know something happened.
 *         // and if/when you don't care anymore
 *         unsubscribe();
 *     }
 * }
 * </pre>
 * </p>
 * 
 * make a version that takes URIs?  or maybe it should only take URIs and convert its
 * paths internally?
 * 
 * @author jason
 *
 */
@ThreadSafe
public abstract class FileWatchSubscription {
	
	static WeakReference<FileWatchService> fileWatchServiceRef;
	
	protected final Path path;
	
	boolean subscribe = true;
	
	/**
	 * Subscribe to notifications when changes occur to a particular path.
	 * If you subscribe to a directory, you'll get notifications for its children.
	 * If you subscribe to a file, only notifications for that file will be delivered.
	 * 
	 * @param paths The filesystem path to watch
	 */
	public FileWatchSubscription(Path path) {
		assert path != null : "need at least one path";
		this.path = path;
		FileWatchService fws = fileWatchServiceRef.get();
		if (this.path.getFileSystem().equals(FileSystemService.defaultFileSystem) && fws != null) {
			fws.requestQueue.offer(this);
		}
	}
	
	/**
	 * Unsubscribe from notifications.  After calling this method, the object cannot
	 * be reused.
	 */
	@NonBlocking
	public final void unsubscribe() {
		subscribe = false;
		FileWatchService fws = fileWatchServiceRef.get();
		if (fws != null) {
			fws.requestQueue.offer(this);
		}
	}
	
	protected abstract void fileChanged(Path path, Kind<Path> operation);
	
	@Override
	public final boolean equals(Object other) {
		return other == this;
	}
	
	@Override
	public final int hashCode() {
		return System.identityHashCode(this);
	}
}
