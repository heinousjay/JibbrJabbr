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

import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.KernelMessages.LoopThreadName;
import static jj.KernelMessages.ObjectDisposing;
import static jj.KernelMessages.ObjectInstantiated;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.AsyncThreadPool;
import jj.KernelControl;
import jj.KernelTask;
import jj.SynchThreadPool;
import jj.api.Blocking;

import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * @author jason
 *
 */
public class FileSystemService {
	
	/**
	 * separates a jar URI into a path to the jar and a path inside the jar
	 * so that a file system instance can be determined
	 * probably just need to split on the !
	 */
	private static final Pattern JAR_URI_PARSER = Pattern.compile("^jar:([^!]+)!(.+)$");
	
	/**
	 * Slightly ugly technique - exposing the instance of the service so the API works,
	 * without strongly keeping it alive
	 * 
	 * there might be a better way, but i think this works
	 */
	private static WeakReference<FileSystemService> fileSystemServiceRef = null;
	
	/**
	 * Helper
	 * @return
	 */
	private static FileSystemService instance() {
		if (fileSystemServiceRef != null) {
			return fileSystemServiceRef.get();
		}
		return null;
	}
	
	static abstract class FileSystemAPI {
		
		volatile boolean active = true;
		
		/**
		 * The thread pool used to submit completion calls for FileSystemAPI calls
		 * that need them.
		 */
		protected final AsyncThreadPool asyncThreadPool;
		
		FileSystemAPI() {
			FileSystemService instance = instance();
			assert instance != null : "FileSystemService must be running to use this object";
			
			this.asyncThreadPool = instance.asyncThreadPool;
		}
		
		/**
		 * Called if the operation failed.  Optional operation
		 */
		protected void failed(final Throwable t) {}
		
		final void callFailed(final Throwable t) {
			
			asyncThreadPool.submit(new Runnable() {
				
				@Override
				public void run() {
					failed(t);
				}
			});
		}
		
		abstract void execute();
		
		public final void cancel() {
			active = false;
			offer();
		}
		
		/**
		 * Offer the instance to the FileSystemService for processing.
		 */
		final void offer() {
			FileSystemService instance = instance();
			if (instance != null) {
				instance.requestQueue.offer(this);
			}
		}
	}
	
	// works closely with the FileSystemService
	// to maintain
	static abstract class UriToPath extends FileSystemAPI {
		
		final URI uri;
		
		UriToPath(final URI uri) {
			this.uri = uri;
			offer();
		}
		
		abstract void path(Path path);
		
		@Override
		final void execute() {
			
			Path path = null;
			
			if (fileSystemServiceRef != null) {
				FileSystemService instance = fileSystemServiceRef.get();
				if (instance != null) {
					 path = instance.pathForURI(uri);
				}
			}
			
			path(path);
			
		}
	}

	/**
	 * Might as well hang onto it, right?
	 */
	static final FileSystem defaultFileSystem = FileSystems.getDefault();
	
	/**
	 * The queue that feeds the event loop 
	 */
	private final LinkedTransferQueue<FileSystemAPI> requestQueue = new LinkedTransferQueue<>();
	
	private final HashMap<String, WeakReference<FileSystem>> fileSystems = new HashMap<>();
	private final ReferenceQueue<FileSystem> fileSystemRefQueue = new ReferenceQueue<>();

	private final LocLogger logger;
	private final AsyncThreadPool asyncThreadPool;
	
	private volatile boolean run = true;
	
	public FileSystemService(
		final SynchThreadPool synchThreadPool,
		final AsyncThreadPool asyncThreadPool,
		final LocLogger logger,
		final MessageConveyor messages
	) {
		// 
		assert synchThreadPool != null : "No synchronous thread pool provided";
		assert asyncThreadPool != null : "No asynchronous thread pool provided";
		assert logger != null : "No logger provided";
		assert messages != null : "No messages provided";
		
		this.logger = logger;
		this.asyncThreadPool = asyncThreadPool;
		
		synchThreadPool.submit(new Worker(messages.getMessage(LoopThreadName, FileSystemService.class.getSimpleName())));
		
		fileSystemServiceRef = new WeakReference<FileSystemService>(this);
		
		logger.trace(ObjectInstantiated, FileSystemService.class.getSimpleName());
	}
	
	@Blocking
	private Path filePath(URI uri) {
		try {
			return defaultFileSystem.getPath(uri.getPath());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Blocking
	private Path pathInJar(URI uri) {
		try {
			Matcher m = JAR_URI_PARSER.matcher(uri.toString());
			m.matches();
			String jarPath = m.group(1);
			String filePath = m.group(2);
			
			WeakReference<FileSystem> ref = fileSystems.get(jarPath);
			@SuppressWarnings("resource") // it's closed later!
			// need to refactor this cause i don't think these are
			// getting closed
			FileSystem jarFileSystem = ref == null ? null : ref.get();
			if (jarFileSystem == null) {
				
				jarFileSystem = FileSystems.newFileSystem(filePath(URI.create(jarPath)), null);
				fileSystems.put(jarPath, new WeakReference<>(jarFileSystem, fileSystemRefQueue));
			}
			
			return jarFileSystem.getPath(filePath);
		} catch (Exception e) {
			return null;
		}
	}
	
	@Blocking
	private Path pathForURI(URI uri) {
		assert (uri != null) : "no URI provided";
		
		// figure out the filesystem it's in
		String scheme = uri.getScheme();
		if ("file".equals(scheme)) {
			return filePath(uri);
		} else if ("jar".equals(scheme)) {
			return pathInJar(uri);
		}
		
		return null;
	}
	
	// should be an event listener!
	public void control(KernelControl control) {
		run = (control != KernelControl.Dispose);
	}
	
	private final class Worker extends KernelTask {
		
		Worker(String name) {
			super(name);
		}
		
		@Override
		protected void execute() throws Exception {
			while (run) {
				
				FileSystemAPI fsa = requestQueue.poll(5, SECONDS);
				
				{
					// clean the queue
					Reference<? extends FileSystem> reference;
					while ((reference = fileSystemRefQueue.poll()) != null) {
						try {
							reference.get().close();
						} catch (Exception e) {
							// blah - nothing.
							// maybe record it?
						}
					}
				}
				
				if (fsa != null) fsa.execute();
			}
		}
		
		/* (non-Javadoc)
		 * @see jj.KernelTask#cleanup()
		 */
		@Override
		protected void cleanup() {
			
			logger.trace(ObjectDisposing, FileSystemService.class.getSimpleName());
			
			for (WeakReference<FileSystem> reference : fileSystems.values()) {
				FileSystem fs = reference.get();
				if (fs != null) {
					try {
						logger.trace("shutting it down!");
						fs.close();
					} catch (IOException ioe) {
						// log it?
					}
				}
			}
		}
	};
}
