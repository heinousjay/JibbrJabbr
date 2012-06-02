package jj;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

/**
 * <p>
 * Provides the file watching service for the JibbrJabbr system.  Register
 * callbacks against files or directories to receive notifications when 
 * changes occur.  This class is built on the JDK {@link WatchService} and
 * so uses very efficient native mechanisms on platforms where that is supported.
 * </p>
 * 
 * <p>
 * Because of implementation details, a given directory may only have 
 * {@link Integer#MAX_VALUE} listeners total for the directory itself and
 * all contained entries.  This is probably more than enough in practical
 * terms.  We are also limited to watching a total of {@link Integer#MAX_VALUE}
 * directories at one time.  This is also probably more than enough.  Naturally
 * the real limits will be somewhat lower, depending on available memory and
 * kernel resources and many other things outside of the control of this class.
 * </p>
 * 
 * <p>
 * For the purposes of allowing requesting threads to carry on immediately, the
 * callback registration operations may have to move into an internally managed
 * thread, since the I/O operations 
 * </p>
 * 
 * <p>
 * It's possible with some additional thought this can be made entirely nonblocking.
 * </p>
 * 
 * <p>
 * This may end up being exposed to user code, or may not be. Haven't really thought
 * all that through yet.
 * </p>
 * @author jason
 *
 */
@ThreadSafe
public class FileWatchService {
	
	/**
	 * <p>
	 * Callback for receiving notifications that a file system
	 * modification has been detected.  This callback will always be invoked in
	 * the asynchronous pool and so should not directly do any work that will
	 * result in potential thread blocking.
	 * </p>
	 * 
	 * <p>
	 * BE CAREFUL! You probably shouldn't do anything in these callbacks
	 * that modifies the file system, lest you trigger an event storm.
	 * This is somewhat less insidious on polling-based implementations
	 * of the underlying JDK7 {@link WatchService}, but it's just a slower death.
	 * </p>
	 * @author jason
	 */
	public static abstract class FileWatchServiceCallback  {
		
		/**
		 * Receive a notification that a path was created.  Only
		 * really applicable when watching a directory? Need to test
		 * @param path The path that was created.
		 */
		public void created(Path path) {}
		
		/**
		 * Receive a notification that a path was deleted. By the time this
		 * method is called, the path no longer refers to an existing file
		 * @param path The path that was deleted.
		 */
		public void deleted(Path path) {}
		
		/**
		 * Receive a notification that a path was modified.
		 * @param path The path that was modified
		 */
		public void modified(Path path) {}
	}
	
	/**
	 * Maintains all of the callbacks for a given directory.
	 * @author jason
	 */
	private final class WatchKeyCallbacks {

		/**
		 * The guard for key manipulations.  The reader lock should be held when updates are
		 * being made to the dependency set for the key, i.e. if the callback maps are being
		 * updated.  The writer lock should be held when attempting to manipulate the key itself
		 * which is to say canceling it.  The listenerCount also get guarded by this lock because
		 * it is used as a quick way of determining if the key can be cancelled.
		 * 
		 * Old notes:
		 * what is being read and written with this lock is the state of the WatchKey
		 * the basic idea is that when there are no callbacks registered for a particular
		 * key, we want to know to cancel it so that we don't get notifications for it anymore
		 * and bother doing all that processing.
		 */
		final ReentrantReadWriteLock keyCancellationLock = new ReentrantReadWriteLock();
		
		/**
		 * The key from the underlying watch service.
		 */
		@GuardedBy("keyCancellationLock")
		final WatchKey key;
		
		// this is atomic despite being guarded because it is
		// reader-writer guarded, but the "readers" actually write
		// to it and we don't want partial updates messing up
		// our flow.  this could maybe be a volatile int - same
		// semantics, i think.  not sure, happy to leave it
		// like this.
		@GuardedBy("keyCancellationLock")
		final AtomicInteger listenerCount = new AtomicInteger(0);
		
		final Path path;
		
		// despite the fact that all access to these maps occurs in the context of the
		// keyCancellationLock, it's only under the read lock, so we keep using a ConcurrentHashMap
		// because my expectation for a common use case here is reloading resources in the application
		// code - which means we'll be "reading" a lot and canceling keys ("writing") rarely, 
		// but writing to these maps somewhat more frequently in circumstances where they might need locks
		// around callback execution submission - which i'd really like to not lock around
		final ConcurrentHashMap<FileWatchServiceCallback, Boolean> directoryCallbacks
			= new ConcurrentHashMap<>();
		final ConcurrentHashMap<Path, ConcurrentHashMap<FileWatchServiceCallback, Boolean>> fileCallbacks
			= new ConcurrentHashMap<>();
			
		WatchKeyCallbacks(Path path, WatchKey key) {
			this.path = path;
			this.key = key;
		}
			
		/**
		 * Adds a callback that listens to the base directory for this listener set.  This
		 * method can spuriously fail because another thread canceled the associated watch key in the meantime.
		 * If that happens this will return false and the caller is responsible for redoing the whole shebang.
		 * probably just in a busy loop
		 * 
		 * @param callback The callback to be notified of changes to this directory
		 * @return true if adding succeeded, or false if adding failed because the key got canceled in the meantime
		 */
		boolean addCallback(FileWatchServiceCallback callback) {
			boolean success = false;
			// the key is in the process of getting cancelled if this fails
			if (keyCancellationLock.readLock().tryLock()) {
				try {
					logger.info("Adding callback for " + path);
					if (key.isValid() && directoryCallbacks.putIfAbsent(callback, Boolean.TRUE) == null) {
						listenerCount.incrementAndGet();
					}
					success = true;
				} finally {
					keyCancellationLock.readLock().unlock();
				}
			}
			// if we added the callback and the key is still valid, we succeeded.
			// see reasoning inside the null callbacks check in serviceRunnable 
			return success && key.isValid();
		}
		
		boolean addCallback(FileWatchServiceCallback callback, Path path) {
			boolean success = false;
			// the key is in the process of getting cancelled if this fails
			if (keyCancellationLock.readLock().tryLock()) {
				try {
					logger.info("Adding callback for " + path);
					fileCallbacks.putIfAbsent(path, new ConcurrentHashMap<FileWatchServiceCallback, Boolean>());
					if (key.isValid() && fileCallbacks.get(path).putIfAbsent(callback, Boolean.TRUE) == null) {
						listenerCount.incrementAndGet();
					}
					success = true;
				} finally {
					keyCancellationLock.readLock().unlock();
				}
			}
			return success && key.isValid();
		}
		
		void removeCallback(FileWatchServiceCallback callback, Path path) {
			// logically, this should never contend since adding and canceling
			// are protected by this lock.  is that true?  i arrived at that
			// conclusion instinctively but protected it anyway
			keyCancellationLock.readLock().lock();
			try {
				if (path.equals(this.path)) {
					if (directoryCallbacks.remove(callback) == Boolean.TRUE) {
						listenerCount.decrementAndGet();
					} 
				} else {
					ConcurrentHashMap<FileWatchServiceCallback, Boolean> inner = fileCallbacks.get(path);
					if (inner != null && inner.remove(callback) == Boolean.TRUE) {
						listenerCount.decrementAndGet();
					}
				}
			} finally {
				keyCancellationLock.readLock().unlock();
			}
		}
		
		/**
		 * dispatches the events as needed.
		 * @param path
		 * @param synchExecutor
		 */
		int dispatch(final Path path, final Kind<Path> kind) {
			int called = 0;
			for (final FileWatchServiceCallback callback : directoryCallbacks.keySet()) {
				++called;
				asyncExecutor.execute(new Runnable() {
					public void run() {
						try {
							if (kind == ENTRY_CREATE) {
								callback.created(path);
							}
							if (kind == ENTRY_DELETE) {
								callback.deleted(path);
							}
							if (kind == ENTRY_MODIFY) {
								callback.modified(path);
							}
						} catch (Exception e) {
							logger.warn("callback threw an exception", e);
						}
					}
				});
			}
			
			ConcurrentHashMap<FileWatchServiceCallback, Boolean> callbacks = fileCallbacks.get(path);
			if (callbacks != null) {
				for (final FileWatchServiceCallback callback : callbacks.keySet()) {
					++called;
					asyncExecutor.execute(new Runnable() {
						public void run() {
							try {
								if (kind == ENTRY_CREATE) {
									callback.created(path);
								}
								if (kind == ENTRY_DELETE) {
									callback.deleted(path);
								}
								if (kind == ENTRY_MODIFY) {
									callback.modified(path);
								}
							} catch (Exception e) {
								logger.warn("callback threw an exception", e);
							}
						}
					});
				}
			}
			return called;
		}
	}
	
	/**
	 * Handles the running tasks of the watch service, taking selected
	 * keys from the service and dispatching interested callbacks
	 */
	private final Runnable serviceRunnable = new Runnable() {

		/**
		 * Strange design decisions haunt the JDK, and so
		 * we must fight the type system to avoid warnings
		 * @param event
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private WatchEvent<Path> cast(WatchEvent<?> event) {
			return (WatchEvent<Path>)event;
		}
		
		@Override
		public void run() {
			try {
				while (run) {
					WatchKey key = watcher.take();
					WatchKeyCallbacks callbacks = keys.get(key);
					// this condition should NEVER happen unless
					// there is a programming error in here or
					// an incredibly unfortunate interleaving occurs
					// with the register method, which is why registrations
					// must be checked for validity when it is all finished
					// and reattempted if it fails
					if (callbacks == null) {
						logger.warn("A key with no callbacks was being watched.  Some resource was mishandled");
						key.cancel();
					} else if (key.isValid()) {
						int totalCalled = 0;
						for (WatchEvent<?> evnt : key.pollEvents()) {
							Kind<?> kind = evnt.kind();
							if (kind == OVERFLOW) {
								logger.info("FileWatchService OVERFLOW");
								continue; // for now.  not sure what else to do
							}
							
							WatchEvent<Path> event = cast(evnt);
							Path path = event.context();
			                Path child = callbacks.path.resolve(path);
			                totalCalled += callbacks.dispatch(child, event.kind());
						}
						
						if (totalCalled == 0) { 
							// this is the thrust of the lock.  in this situation, we need
							// an accurate count of the number of listeners associated to
							// this key so we can stop wasting resources on it
							// it's okay to aggressively release here, if some thread was contending
							// to add a listener, the key will get recreated, and in the common
							// case where this is no contention, it's all reader locks and
							// concurrent hash maps hap-hap-happily rolling along
							// ideally this lock is as fine grained as possible.  contention
							// here should be EXTREMELY rare.
							callbacks.keyCancellationLock.writeLock().lock();
							try {
				                if (callbacks.listenerCount.get() == 0) {
				                	key.cancel();
				                }
							} finally {
								callbacks.keyCancellationLock.writeLock().unlock();
							}
						}
					}
		            if (!key.reset()) {
		                keys.remove(key);
		            }
				}
			} catch (ClosedWatchServiceException closed) {
				// empty - we have been shutdown, say goodnight gracie
				logger.debug("FileWatchService received ClosedWatchServiceException");
			} catch (InterruptedException ie) {
				// something canceled us somehow,
				// set the interrupt flag and exit
				Thread.currentThread().interrupt();
				shutdown(); // for clean up
			}
			
		}
	};
	
	private final ConcurrentHashMap<WatchKey, WatchKeyCallbacks> keys;
	private final FileSystem fileSystem;
	private final WatchService watcher;
	private final LocLogger logger;
	private final SynchThreadPool synchExecutor;
	private final AsyncThreadPool asyncExecutor;
	
	private volatile boolean run = true;
	
	/**
	 * Constructor that takes all dependencies
	 * @param synchExecutor
	 * @param logger
	 */
	public FileWatchService(
		final SynchThreadPool synchExecutor,
		final AsyncThreadPool asyncExecutor,
		final LocLogger logger,
		final MessageConveyor messages
	) {
		// 
		assert synchExecutor != null : "No synchronous executor provided";
		assert asyncExecutor != null : "No asynchronous executor provided";
		assert logger != null : "No logger provided";
		assert messages != null : "No messages provided";
		
		try {
			this.keys = new ConcurrentHashMap<>();
			this.fileSystem = FileSystems.getDefault();
			this.watcher = this.fileSystem.newWatchService();
			this.logger = logger;
			this.synchExecutor = synchExecutor;
			this.asyncExecutor = asyncExecutor;
		} catch (IOException e) {
			// TODO throw a smarter exception here
			throw new IllegalStateException("", e);
		}
		// very definitely a synchronous task
		synchExecutor.submit(serviceRunnable);
	}
	
	/**
	 * Finds (and creates if necessary) the WatchKey and WatchKeyCallbacks for a given directory.
	 * @param directory The directory to be watched
	 * @return A WatchKeyCallbacks instance for the directory, freshly created if necessary
	 * @throws IOException If registering the WatchKey fails
	 */
	private WatchKeyCallbacks register(final Path directory) throws IOException {
		WatchKey watchKey = directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		keys.putIfAbsent(watchKey, new WatchKeyCallbacks(directory, watchKey));
		return keys.get(watchKey);
	}
	
	/**
	 * Synchronously register a path for watching.  This method interrogates the file system
	 * and therefore should not be called from an asynchronous context.
	 * @param path
	 * @param callback
	 * @return
	 */
	public boolean watch(final Path path, final FileWatchServiceCallback callback) throws IOException {
		if (path == null) throw new IllegalArgumentException("");
		if (callback == null) throw new IllegalArgumentException("");
		if (path.getFileSystem() != fileSystem) throw new IllegalArgumentException("");
		boolean success = false;
		// if it fails, that means the WatchKey was invalidated while
		// adding the callback, and we need to try again.
		while (!success) { 
			// is this path a file? then we register the parent first
			// then register the path
			if (Files.isDirectory(path)) {
				success = register(path).addCallback(callback);
			} else {
				success = register(path.getParent()).addCallback(callback, path);
			}
		}
		return success;
	}
	
	/**
	 * Asynchronously register a path for watching, invoking the appropriate method on response when finished.
	 * This method returns immediately without having completed registration and notifies the supplied response
	 * callback upon completion
	 * @param path
	 * @param callback
	 * @param response
	 * @throws IllegalArgumentException immediately if any parameters are null
	 */
	public void watch(final Path path, final FileWatchServiceCallback callback, final SynchronousOperationCallback<Boolean> response) {
		if (path == null) throw new IllegalArgumentException("");
		if (callback == null) throw new IllegalArgumentException("");
		if (response == null) throw new IllegalArgumentException("");
		synchExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					response.invokeComplete(asyncExecutor, Boolean.valueOf(watch(path, callback)));
				} catch (final Throwable t) {
					response.invokeThrowable(asyncExecutor, t);
				}
			}
		});
	}
	
	public void stopWatching(final Path path, final FileWatchServiceCallback callback) throws IOException {
		if (path == null) throw new IllegalArgumentException("");
		if (callback == null) throw new IllegalArgumentException("");

		WatchKey key;
		if (Files.isDirectory(path)) {
			key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		} else {
			key = path.getParent().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		}
		WatchKeyCallbacks callbacks = keys.get(key);
		if (callbacks != null) {
			callbacks.removeCallback(callback, path);
		}
	}
	
	public void stopWatching(final Path path, final FileWatchServiceCallback callback, final SynchronousOperationCallback<Void> response) {
		if (path == null) throw new IllegalArgumentException("");
		if (callback == null) throw new IllegalArgumentException("");
		if (response == null) throw new IllegalArgumentException("");
		synchExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					stopWatching(path, callback);
					response.invokeComplete(asyncExecutor, null);
				} catch (Throwable t) {
					response.invokeThrowable(asyncExecutor, t);
				}
			}
		});
	}
	
	/**
	 * Shuts the FileWatchService down.  After calling this, this instance is
	 * unusable and must be reinitialized.  In order to support pausing and
	 * restarting the service, there are some refactorings that can be done, and might
	 * need to be.
	 */
	public void shutdown() {
		try {
			run = false;
			watcher.close();
		} catch (Exception e) {
			// log that something went wrong but eat it
			logger.warn("Exception occurred shutting down FileWatchService", e);
		}
	}
}
