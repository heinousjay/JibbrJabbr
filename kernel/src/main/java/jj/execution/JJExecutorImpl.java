package jj.execution;

import java.util.concurrent.DelayQueue;
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
class JJExecutorImpl implements JJExecutor {

	private final ExecutorBundle bundle;
	private final CurrentTask currentTask;
	private final Logger logger;
	
	private final Thread monitorThread;
	private final DelayQueue<JJTask> queuedTasks = new DelayQueue<>();
	
	private final Runnable monitor = new Runnable() {
		
		@Override
		public void run() {
			try {
				while (true) {
					JJTask task = queuedTasks.take();
					System.err.println(task + " has been waiting too long to execute!");
				}
			} catch (InterruptedException e) {
				// just let the hate flow...
				// for now, anyway.  maybe this belongs in the loop
			}
		}
	};
	
	@Inject
	JJExecutorImpl(
		final ExecutorBundle bundle,
		final CurrentTask currentTask,
		final @EmergencyLogger Logger logger
	) {
		this.bundle = bundle;
		this.currentTask = currentTask;
		this.logger = logger;
		
		this.monitorThread = new Thread(monitor, "Task execution monitor");
		this.monitorThread.setDaemon(true);
		this.monitorThread.start();
	}
	
	@Override
	public Future<?> execute(final JJTask task) {
		
		task.enqueue();
		queuedTasks.add(task);
		
		return task.executor(bundle).submit(new Runnable() {
			
			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				Thread.currentThread().setName(name + " - " + task.name());
				currentTask.set(task);
				queuedTasks.remove(task);
				task.start();
				try {
					task.run();
				} catch (Throwable t) {
					logger.error("Exception caught in executor", t);
				} finally {
					task.end();
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
