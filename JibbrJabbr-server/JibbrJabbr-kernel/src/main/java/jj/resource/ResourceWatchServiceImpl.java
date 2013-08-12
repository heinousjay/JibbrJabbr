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

import jj.JJServerShutdownListener;
import jj.JJServerStartupListener;
import jj.execution.JJExecutors;
import jj.execution.JJRunnable;

/**
 * watches for file changes on resources we've already loaded
 * @author jason
 *
 */
@Singleton
class ResourceWatchServiceImpl implements ResourceWatchService, JJServerStartupListener, JJServerShutdownListener {
	
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
	
	@Override
	public void start() {
		executors.ioExecutor().submit(loop);
	}
	
	@Override
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
		
		private void remove(final AbstractResource resource) {
			log.info("removing {}", resource);
			if (resourceCache.remove(resource.cacheKey(), resource)) {
				for (AbstractResource dependent : resource.dependents()) {
					reload(dependent);
				}
			}
		}
		
		private void reload(final AbstractResource resource) {
			// the resources are reloaded as a separate task so we don't clog up
			// the reloader thread doing other work
			resource.markObselete();
			executors.ioExecutor().submit(
				new JJRunnable(ResourceWatchService.class.getSimpleName() + " reloader for " + resource.baseName()) {
				
					@Override
					protected boolean ignoreInExecutionTrace() {
						return true;
					}

					@Override
					public void run() {
						log.info("reloading {}", resource);
						resourceFinder.loadResource(
							resource.getClass(),
							resource.baseName(),
							resource.creationArgs()
						);
						for (AbstractResource dependent : resource.dependents()) {
							reload(dependent);
						}
					}
				}
			);
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
								for (final Resource resource : resourceCache.findAllByUri(path.toUri())) {
									remove((AbstractResource)resource);
								}
							
							} else if (kind == ENTRY_MODIFY) {
								for (final Resource resource : resourceCache.findAllByUri(path.toUri())) {
									reload((AbstractResource)resource);
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
