package jj.resource;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.execution.JJExecutors;
import jj.execution.JJRunnable;

/**
 * watches for file changes on resources we've already loaded
 * @author jason
 *
 */
@Singleton
class ResourceWatchServiceImpl implements ResourceWatchService {
	
	private final Logger log = LoggerFactory.getLogger(ResourceWatchServiceImpl.class);
	
	private final WatchService watcher;
	
	private final ResourceCache resourceCache;
	
	private final ResourceFinder resourceFinder;
	
	private final JJExecutors executors;
	
	@Inject
	ResourceWatchServiceImpl(
		final ResourceCache resourceCache,
		final ResourceFinder resourceFinder,
		final JJExecutors executors
	) throws IOException {
		this.resourceCache = resourceCache;
		this.resourceFinder = resourceFinder;
		this.executors = executors;
		watcher = FileSystems.getDefault().newWatchService();
	}

	@Override
	public void watch(Resource resource) throws IOException {
		
		final Path directory = ((AbstractResource)resource).path().getParent();
		if (directory.getFileSystem() == FileSystems.getDefault()) {
			log.trace("registering for watch service: {}", resource);
			directory.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);
		}
	}
	
	public void start() {
		executors.ioExecutor().submit(loop);
	}
	
	public void stop() {
		try {
			watcher.close();
		} catch (IOException e) {}
	}
	
	@SuppressWarnings("unchecked")
	private WatchEvent<Path> cast(WatchEvent<?> event) {
		return (WatchEvent<Path>)event;
	}
		
	private final JJRunnable loop = new JJRunnable(ResourceWatchService.class.getSimpleName() + " loop") {

		@Override
		protected boolean ignoreInExecutionTrace() {
			return true;
		}
		
		@Override
		public void run() {
			try {
				// we're a daemon thread, run till we can't run no more
				// or get interrupted, whichever comes first
				while (true) {
					WatchKey watchKey = watcher.take();
					if (watchKey.isValid()) {
						final Path directory = (Path)watchKey.watchable();
						
						for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
							
							final Kind<?> kind = watchEvent.kind();
							if (kind == OVERFLOW) {
								log.warn("FileWatchService OVERFLOW - not sure what this means!");
								continue; // for now. not sure what else to do
							}
							final WatchEvent<Path> event = cast(watchEvent);
							final Path context = event.context();
							final Path path = directory.resolve(context);
							
							if (kind == ENTRY_DELETE) {
								// it'll get reloaded if it gets recreated
								// later and someone wants it
								log.info("removing {}", path);
								resourceCache.removeAllByUri(path.toUri());
							
							} else if (kind == ENTRY_MODIFY) {
								for (final Resource resource : resourceCache.findAllByUri(path.toUri())) {
									// this thread is only to handle the
									// WatchEvents
									executors.ioExecutor().submit(
										new JJRunnable(ResourceWatchService.class.getSimpleName() + " reloader for " + resource.baseName()) {
										
											@Override
											protected boolean ignoreInExecutionTrace() {
												return true;
											}
		
											@Override
											public void run() {
												log.info("reloading {}",path);
												resourceFinder.loadResource(
													resource.getClass(),
													resource.baseName(),
													resource.creationArgs()
												);
											}
										}
									);
								}
							}
						}
						
						if (!Files.exists(directory)) {
							watchKey.cancel();
						}
					}
					
					watchKey.reset();
				}
			// this is the quit signal
			} catch (ClosedWatchServiceException | InterruptedException e) {
			} catch (Exception other) {
				log.error("Exception thrown in watch service", other);
			}
		}
	};
}
