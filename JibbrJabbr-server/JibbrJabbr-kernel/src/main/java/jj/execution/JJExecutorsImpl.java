package jj.execution;

import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import jj.logging.EmergencyLogger;

/**
 * exposes some execution related information and
 * provides the API for execution services in the
 * system.  DO NOT DEPEND ON THIS DIRECTLY, use the
 * interface
 * 
 * @author jason
 *
 */
@Singleton
class JJExecutorsImpl implements JJExecutors {

	private final ExecutorBundle bundle;
	private final CurrentTask currentTask;
	private final Logger logger;
	
	@Inject
	public JJExecutorsImpl(
		final ExecutorBundle bundle,
		final CurrentTask currentTask,
		final @EmergencyLogger Logger logger
	) {
		this.bundle = bundle;
		this.currentTask = currentTask;
		this.logger = logger;
	}
	
	@Override
	public Future<?> execute(final JJTask task) {
		
		return task.executor(bundle).submit(new Runnable() {
			
			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				Thread.currentThread().setName(name + " - " + task.name());
				currentTask.set(task);
				try {
					task.run();
				} catch (Throwable t) {
					logger.error("Exception caught in executor", t);
				} finally {
					Thread.currentThread().setName(name);
				}
			}
		});
	}
	
	@Override
	public boolean isScriptThread() {
		return bundle.scriptExecutorFactory.isScriptThread();
	}
	
	@Override
	public boolean isScriptThreadFor(String baseName) {
		return bundle.scriptExecutorFactory.isScriptThreadFor(baseName);
	}

	public boolean isIOThread() {
		return IOExecutor.isIOThread();
	}
	
	// TODO replace the use of this with configuration
	public int ioPoolSize() {
		return bundle.ioExecutor.getMaximumPoolSize();
	}
}
