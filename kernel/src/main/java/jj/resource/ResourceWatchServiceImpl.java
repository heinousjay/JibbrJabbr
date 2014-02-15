package jj.resource;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.JJServerStartupListener;
import jj.configuration.Arguments;
import jj.execution.TaskRunner;

/**
 * watches for file changes on resources we've already loaded
 * @author jason
 *
 */
@Singleton
class ResourceWatchServiceImpl implements ResourceWatchService, JJServerStartupListener {
	
	private final TaskRunner taskRunner;
	
	private final boolean run;
	
	private final ResourceWatchServiceLoop loop;
	
	@Inject
	ResourceWatchServiceImpl(
		final TaskRunner taskRunner,
		final Arguments arguments,
		final Provider<ResourceWatchServiceLoop> provider
	) {
		this.taskRunner = taskRunner;
		run = arguments.get("fileWatcher", boolean.class, true);
		loop = run ? provider.get() : null;
	}

	@Override
	public void watch(FileResource resource) throws IOException {
		if (run) {
			loop.watch(resource);
		}
	}
	
	@Override
	public void start() {
		if (run) {
			taskRunner.execute(loop);
		}
	}
	
	@Override
	public Priority startPriority() {
		return Priority.NearHighest;
	}
}
