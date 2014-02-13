package jj.execution;

import java.util.List;
import java.util.concurrent.DelayQueue;
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
class TaskRunnerImpl implements TaskRunner {
	
	private static final long MAX_QUEUED_TIME = TimeUnit.SECONDS.toMillis(10);

	private final ExecutorBundle executors;
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
					System.err.println(task + " has been waiting " + MAX_QUEUED_TIME + " milliseconds to execute.  something is broken");
				}
			} catch (InterruptedException e) {
				// if we get interrupted, just end
			}
		}
	};
	
	@Inject
	TaskRunnerImpl(
		final ExecutorBundle bundle,
		final CurrentTask currentTask,
		final @EmergencyLogger Logger logger
	) {
		this.executors = bundle;
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
	public Promise execute(final JJTask task) {
		
		task.enqueue(MAX_QUEUED_TIME);
		queuedTasks.add(task);
		
		final Promise promise = task.promise().taskRunner(this);
		
		task.addRunnableToExecutor(executors, new Runnable() {
			
			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				Thread.currentThread().setName(name + " - " + task.name());
				queuedTasks.remove(task);
				task.start();
				try (Closer closer = currentTask.enterScope(task)) {
					try {
						task.run();
					} catch (Throwable t) {
						if (!task.errored(t)) {
							logger.error("Task [{}] ended in exception", task.name());
							logger.error("", t);
						}
					}
				} finally {
					task.end();
					Thread.currentThread().setName(name);
					
					List<JJTask> tasks = promise.done();
					if (tasks != null) {
						for (JJTask t : tasks) {
							execute(t);
						}
					}
				}
			}
		});
		
		return promise;
	}

	public boolean isIOThread() {
		return IOExecutor.isIOThread();
	}
}
