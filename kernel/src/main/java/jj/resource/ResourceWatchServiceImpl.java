package jj.resource;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.configuration.ConfigurationLoaded;
import jj.event.Listener;
import jj.event.Subscriber;

/**
 * manages the resource watch service by bridging into the configuration
 * for lifecycle control and provides an endpoint for allowing file watching
 * @author jason
 *
 */
@Singleton
@Subscriber
class ResourceWatchServiceImpl implements ResourceWatchService {
	
	private final ResourceWatchSwitch resourceWatchSwitch;
	
	private final ResourceWatchServiceLoop loop;
	
	@Inject
	ResourceWatchServiceImpl(
		ResourceWatchSwitch resourceWatchSwitch,
		ResourceWatchServiceLoop loop
	) {
		this.resourceWatchSwitch = resourceWatchSwitch;
		this.loop = loop;
	}

	@Override
	public void watch(FileSystemResource resource) throws IOException {
		if (resourceWatchSwitch.runFileWatcher()) {
			loop.watch(resource);
		}
	}
	
	@Listener
	void on(ConfigurationLoaded event) {
		if (resourceWatchSwitch.runFileWatcher()) {
			loop.start();
		} else {
			loop.stop();
		}
	}
}
