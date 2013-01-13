package jj.resource;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jj.JJExecutors;
import jj.JJRunnable;
import jj.JJShutdown;

/**
 * watches for file changes on resources we've already loaded
 * @author jason
 *
 */
class ResourceWatchService implements JJShutdown {
	
	private final Logger log = LoggerFactory.getLogger(ResourceWatchService.class);
	
	private final WatchService watcher;
	
	private final ResourceCache resourceCache;
	
	private final ResourceFinder resourceFinder;
	
	private final ExecutorService ioExecutor;
	
	ResourceWatchService(
		final ResourceCache resourceCache,
		final ResourceFinder resourceFinder,
		final JJExecutors executors
	) throws IOException {
		this.resourceCache = resourceCache;
		this.resourceFinder = resourceFinder;
		this.ioExecutor = executors.ioExecutor();
		watcher = FileSystems.getDefault().newWatchService();
		ioExecutor.submit(loop);
	}

	void watch(Resource resource) throws IOException {
		
		final Path directory = resource.path().getParent();
		if (directory.getFileSystem() == FileSystems.getDefault()) {
			log.trace("registering for watch service: {}", resource);
			directory.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);
		}
	}
	
	public void stop() {
		log.info("stopping the resource watch service");
		try {
			watcher.close();
		} catch (IOException e) {}
	}
	
	@SuppressWarnings("unchecked")
	private WatchEvent<Path> cast(WatchEvent<?> event) {
		return (WatchEvent<Path>)event;
	}
		
	private final JJRunnable loop = new JJRunnable("ResourceWatchService loop") {
		
		@Override
		protected void innerRun() throws Exception {
			// we're a daemon thread, run till we can't run no more
			// or get interrupted, whichever comes first
			while (true) {
				WatchKey watchKey = watcher.take();
				if (watchKey.isValid()) {
					final Path directory = (Path)watchKey.watchable();
					for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
						
						final Kind<?> kind = watchEvent.kind();
						if (kind == OVERFLOW) {
							log.info("FileWatchService OVERFLOW - not sure what this means!");
							continue; // for now. not sure what else to do
						}
						final WatchEvent<Path> event = cast(watchEvent);
						final Path context = event.context();
						final Path path = directory.resolve(context);
						
						if (kind == ENTRY_DELETE) {
							// it'll get reloaded if it gets recreated
							// later and someone wants it
							log.info("removing {}", path);
							resourceCache.remove(path.toUri());
						} else if (kind == ENTRY_MODIFY) {
							log.info("reloading {}",path);
							final Resource resource = resourceCache.get(path.toUri());
							if (resource != null) {
								// this thread is only to handle the
								// WatchEvents
								ioExecutor.submit(new JJRunnable("ResourceWatchService reloader") {

									@Override
									protected void innerRun() throws Exception {
										resourceFinder.loadResource(
											resource.getClass(),
											resource.baseName(),
											resource.creationArgs()
										);
									}
								});
							}
						}
						
						if (!Files.exists(directory)) {
							watchKey.cancel();
						}
					}
				}
				watchKey.reset();
			}
		}
	};
}
