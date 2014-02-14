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
	
	private final DelayQueue<JJTask> queuedTasks = new DelayQueue<>();
	
	private final ServerTask monitor = new ServerTask(getClass().getSimpleName() + " execution monitor") {
		
		@Override
		public void run() throws Exception {
			while (true) {
				JJTask task = queuedTasks.take();
				// TODO - do something more interesting than this! after all at this point it's an issue, i'm sure
				System.err.println(task + " has been waiting " + MAX_QUEUED_TIME + " milliseconds to execute.  something is broken");
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
		
		execute(monitor);
	}
	
	@Override
	public Promise execute(final JJTask task) {
		
		task.enqueue(MAX_QUEUED_TIME);
		queuedTasks.add(task);
		
		final Promise promise = task.promise().taskRunner(this);
		
		task.addRunnableToExecutor(executors, new Runnable() {
			
			@Override
			public void run() {
				boolean interrupted = false;
				String name = Thread.currentThread().getName();
				Thread.currentThread().setName(name + " - " + task.name());
				queuedTasks.remove(task);
				task.start();
				try (Closer closer = currentTask.enterScope(task)) {
					task.run();
				} catch (InterruptedException ie) {
					interrupted = true;
				} catch (Throwable t) {
					if (!task.errored(t)) {
						logger.error("Task [{}] ended in exception", task.name());
						logger.error("", t);
					}

				} finally {
					task.end();
					Thread.currentThread().setName(name);
					
					// interruption means shutdown, don't bother keeping promises now
					if (!interrupted) {
						List<JJTask> tasks = promise.done();
						if (tasks != null) {
							for (JJTask t : tasks) {
								execute(t);
							}
						}
					}
				}
			}
		});
		
		return promise;
	}
}
