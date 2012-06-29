package jj.io;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.KernelMessages.LoopThreadName;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedTransferQueue;

import jj.AsyncThreadPool;
import jj.SynchThreadPool;
import jj.api.Blocking;
import jj.api.NonBlocking;

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
 * @author jason
 *
 */
@ThreadSafe
public class FileWatchService {
	
	/**
	 * transfers the subscription requests into the event loop
	 * 
	 * is package friendly so that instantiating the FileWatchSubscription causes subscription.  talk
	 * about a nice API
	 */
	final LinkedTransferQueue<FileWatchSubscription> requestQueue = new LinkedTransferQueue<>();
	
	private final HashMap<WatchKey, WeakHashMap<FileWatchSubscription, Boolean>> keys = new HashMap<>();
	private final WatchService watcher = FileSystemService.fileSystem.newWatchService();
	private final LocLogger logger;
	private final MessageConveyor messages;
	private final AsyncThreadPool asyncExecutor;
	
	private volatile boolean run = true;
	
	/**
	 * Constructor that takes all dependencies
	 * @param synchExecutor
	 * @param logger
	 */
	@NonBlocking
	public FileWatchService(
		final SynchThreadPool synchExecutor,
		final AsyncThreadPool asyncExecutor,
		final LocLogger logger,
		final MessageConveyor messages
	) throws Exception {
		// 
		assert synchExecutor != null : "No synchronous executor provided";
		assert asyncExecutor != null : "No asynchronous executor provided";
		assert logger != null : "No logger provided";
		assert messages != null : "No messages provided";
		
		this.logger = logger;
		this.asyncExecutor = asyncExecutor;
		this.messages = messages;
		
		// very definitely a synchronous task
		synchExecutor.submit(serviceRunnable);
		
		FileWatchSubscription.fileWatchServiceRef = new WeakReference<FileWatchService>(this);
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
		@NonBlocking
		private WatchEvent<Path> cast(WatchEvent<?> event) {
			return (WatchEvent<Path>)event;
		}
		
		@Override
		@Blocking
		public void run() {
			String name = Thread.currentThread().getName();
			Thread.currentThread().setName(messages.getMessage(LoopThreadName, FileWatchService.class.getSimpleName()));
			try {
				while (run) {
					// wake up every now and again to register new watched files even if 
					// no events are coming.
					// the time can come from settings.  why not.
					// for now once a second, just to see
					final WatchKey key = watcher.poll(1, SECONDS);
					
					if (run) {
						FileWatchSubscription fws;
						while ((fws = requestQueue.poll()) != null) {
							
							try {
								final Path path = fws.path;
								final Path directory = Files.isDirectory(path) ? path : path.getParent();
								final WatchKey fwsKey = directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
								
								if (fws.subscribe) {
									WeakHashMap<FileWatchSubscription, Boolean> subscribers = keys.get(fwsKey);
									if (subscribers == null) {
										subscribers = new WeakHashMap<>();
										keys.put(fwsKey, subscribers);
									}
									subscribers.put(fws, Boolean.TRUE);
								} else {
									final WeakHashMap<FileWatchSubscription, Boolean> subscribers = keys.get(fwsKey);
									subscribers.remove(fws);
									if (subscribers.isEmpty()) {
										keys.remove(fwsKey);
										fwsKey.cancel();
									}
								}
							} catch (final IOException io) {
								// publish the exception
								io.printStackTrace();
							}
							// we check inside the loop because we're registering watchers up in here
							if (Thread.interrupted()) throw new InterruptedException();
						}
					}
					
					if (key != null) {
						if (key.isValid()) {
							final WeakHashMap<FileWatchSubscription, Boolean> subscribers = keys.get(key);
							if (subscribers == null) {
								key.cancel();
								logger.warn("A key with no callbacks was being watched.  Some resource was mishandled");
							} else {
								
								for (WatchEvent<?> evnt : key.pollEvents()) {
									final Path watchable = (Path)key.watchable();
									if (key.isValid()) {
										final Kind<?> kind = evnt.kind();
										if (kind == OVERFLOW) {
											logger.info("FileWatchService OVERFLOW");
											continue; // for now.  not sure what else to do
										}
										final WatchEvent<Path> event = cast(evnt);
										final Path context = event.context();
										final Path path = watchable.resolve(context);
										for (final FileWatchSubscription subscriber : subscribers.keySet()) {
											// if the subscription is to the directory, all event are noted
											// if the subscription is to a specific file only events for that file are noted
											if (Files.isDirectory(subscriber.path) || path.equals(subscriber.path)) {
												asyncExecutor.submit(new Runnable() {
													@Override
													public void run() {
														subscriber.fileChanged(path, event.kind());
													}
												});
											}
										}
									}
									// if our directory got deleted out from under us,
									// bye bye
									if (!Files.exists(watchable)) {
										key.cancel();
										keys.remove(key);
									}
								}
							}
						}
						
						if (!key.reset()) {
							keys.remove(key);
						}
					}
				}
			} catch (final ClosedWatchServiceException closed) {
				// empty - we have been shutdown, say goodnight gracie
				logger.debug("FileWatchService received ClosedWatchServiceException");
			} catch (final InterruptedException ie) {
				// something canceled us somehow,
				// set the interrupt flag and exit
				Thread.currentThread().interrupt();
				cleanup();
			}
			Thread.currentThread().setName(name);
		}
	};
	
	
	private void cleanup() {
		try {
			run = false;
			watcher.close();
		} catch (Exception e) {
			// don't care
		}
	}
}
