package jj.execution;

import java.util.List;
import java.util.concurrent.DelayQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.logging.Emergency;
import jj.util.Clock;
import jj.util.Closer;

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

	private final ExecutorBundle executors;
	private final CurrentTask currentTask;
	private final Publisher publisher;
	private final Clock clock;
	
	private final DelayQueue<TaskTracker> queuedTasks = new DelayQueue<>();
	
	private final ServerTask monitor = new ServerTask(getClass().getSimpleName() + " execution monitor") {
		
		private void log(TaskTracker taskTracker, JJTask task) {
			publisher.publish(new Emergency(
				"{} has been waiting {} milliseconds to execute.  something is broken", task, new Long(clock.time() + taskTracker.enqueuedTime()) 
			));
		}
		
		@Override
		public void run() throws Exception {
			while (true) {
				final TaskTracker taskTracker = queuedTasks.take();
				final JJTask task = taskTracker.task();
				if (taskTracker.startTime() == 0 
					&& task != null
					&& ((task instanceof DelayedTask<?>) ? !((DelayedTask<?>)task).cancelKey().canceled() : true)
				) {
					log(taskTracker, task);
				}
			}
		}
	};
	
	@Inject
	TaskRunnerImpl(
		final ExecutorBundle bundle,
		final CurrentTask currentTask,
		final Publisher publisher,
		final Clock clock
	) {
		this.executors = bundle;
		this.currentTask = currentTask;
		this.publisher = publisher;
		this.clock = clock;
		
		execute(monitor);
	}
	
	@Override
	public Promise execute(final JJTask task) {

		final Promise promise = task.promise().taskRunner(this);
		final TaskTracker tracker = new TaskTracker(clock, task);
		
		tracker.enqueue();
		queuedTasks.add(tracker);
		
		// sometimes, the injecting happens manually
		
		task.addRunnableToExecutor(executors, new Runnable() {
			
			@Override
			public void run() {
				
				String oldName = Thread.currentThread().getName();
				String threadName = oldName + " - " + task.name();
				Thread.currentThread().setName(threadName);
				
				
					
					boolean interrupted = false;
					queuedTasks.remove(tracker);
					tracker.start();
					
					try (Closer closer = currentTask.enterScope(task)) {
						task.run();
					} catch (InterruptedException ie) {
						interrupted = true;
					} catch (OutOfMemoryError e) {
						throw e; // just in case
					} catch (Throwable t) {
						if (!task.errored(t)) {
							publisher.publish(new Emergency("Task [" + task.name() + "] ended in exception", t));
							tracker.endedInError();
						}
	
					} finally {
						
						tracker.end();
						
						// interruption means shutdown, don't bother keeping promises
						if (!interrupted) {
							List<JJTask> tasks = promise.done();
							if (tasks != null) {
								for (JJTask t : tasks) {
									execute(t);
								}
							}
						}
						
						publisher.publish(tracker);
						Thread.currentThread().setName(oldName);
						
					}

			}
		});
		
		return promise;
	}
}
