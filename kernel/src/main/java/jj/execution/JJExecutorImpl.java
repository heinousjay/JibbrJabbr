package jj.execution;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import jj.Closer;
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
	
	private static final long MAX_QUEUED_TIME = TimeUnit.SECONDS.toMillis(20);

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
					// TODO - do something more interesting than this! after all at this point it's an issue, i'm sure
					System.err.println(task + " has been waiting too long to execute! not sure what else to say but DAMN baby");
				}
			} catch (InterruptedException e) {
				// if we get interrupted, just end
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
		
		// TODO - make a new set of executors for internal stuff,
		// like this, the file watcher, and there's another lil daemon out there somewhere
		// and just make InternalTask to handle them
		// should allow unlimited thread of execution
		this.monitorThread = new Thread(monitor, "Task execution monitor");
		this.monitorThread.setDaemon(true);
		this.monitorThread.start();
	}
	
	@Override
	public Future<?> execute(final JJTask task) {
		
		task.enqueue(MAX_QUEUED_TIME);
		queuedTasks.add(task);
		
		return task.executor(bundle).submit(new Runnable() {
			
			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				Thread.currentThread().setName(name + " - " + task.name());
				queuedTasks.remove(task);
				task.start();
				try (Closer closer = currentTask.enterScope(task)) {
					task.run();
				} catch (Throwable t) {
					logger.error("Task [{}] ended in exception", task.name());
					logger.error("", t);
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
	// TODO along with that, rework the executors to be restartable
	// so that the size configurations can change at runtime for tuning
	public int ioPoolSize() {
		return bundle.ioExecutor.getMaximumPoolSize();
	}
}
