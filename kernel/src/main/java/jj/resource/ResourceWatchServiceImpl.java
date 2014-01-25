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
import jj.execution.IOTask;
import jj.execution.JJExecutor;

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
	
	private final JJExecutor executors;
	
	@Inject
	ResourceWatchServiceImpl(
		final ResourceCache resourceCache,
		final ResourceFinder resourceFinder,
		final JJExecutor executors
	) throws IOException {
		this.resourceCache = resourceCache;
		this.resourceFinder = resourceFinder;
		this.executors = executors;
		watcher = FileSystems.getDefault().newWatchService();
	}

	@Override
	public void watch(FileResource resource) throws IOException {
		
		final Path directory = resource.path().getParent();
		if (directory.getFileSystem() == FileSystems.getDefault()) {
			log.trace("registering for watch service: {}", resource);
			directory.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);
		}
	}
	
	@Override
	public void start() {
		executors.execute(loop);
	}
	
	@Override
	public Priority startPriority() {
		return Priority.NearHighest;
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
		
	private final IOTask loop = new IOTask(ResourceWatchService.class.getSimpleName() + " loop") {

		
		private void remove(final AbstractResource resource) {
			log.info("removing {}", resource);
			if (resourceCache.remove(resource.cacheKey(), resource)) {
				resource.kill();
				for (AbstractResource dependent : resource.dependents()) {
					reload(dependent);
				}
			}
		}
		
		private void reload(final AbstractResource resource) {
			// the resources are reloaded as a separate task so we don't clog up
			// the reloader thread doing other work
			resource.kill();
			executors.execute(
				new IOTask(ResourceWatchService.class.getSimpleName() + " reloader for " + resource.cacheKey()) {


					@Override
					public void run() {
						log.info("reloading {}", resource);
						resourceFinder.loadResource(
							resource.getClass(),
							resource.baseName(),
							resource.creationArgs()
						);
						for (AbstractResource dependent : resource.dependents()) {
							if (dependent.alive()) reload(dependent);
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