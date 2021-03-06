package jj.execution;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.DelayQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerLifecycle;
import jj.event.Publisher;
import jj.logging.Emergency;
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

	private final Executors executors;
	private final CurrentTask currentTask;
	private final Publisher publisher;
	private final Clock clock;
	private final JJServerLifecycle lifecycle;
	
	private final DelayQueue<TaskTracker> queuedTasks = new DelayQueue<>();
	
	private final ServerTask monitor = new ServerTask(getClass().getSimpleName() + " execution monitor") {
		
		private void log(TaskTracker taskTracker, JJTask<?> task) {
			publisher.publish(new Emergency(
				"{} has been waiting {} milliseconds to execute.  something is broken", task, clock.millis() + taskTracker.enqueuedTime()
			));
		}
		
		@Override
		public void run() throws Exception {
			while (true) {
				final TaskTracker taskTracker = queuedTasks.take();
				final JJTask<?> task = taskTracker.task();
				if (taskTracker.startTime() == 0 
					&& task != null
					&& ((!(task instanceof DelayedTask<?>)) || !((DelayedTask<?>) task).cancelKey().canceled())
				) {
					log(taskTracker, task);
				}
			}
		}
	};
	
	@Inject
	TaskRunnerImpl(
		Executors bundle,
		CurrentTask currentTask,
		Publisher publisher,
		Clock clock,
		JJServerLifecycle lifecycle
	) {
		this.executors = bundle;
		this.currentTask = currentTask;
		this.publisher = publisher;
		this.clock = clock;
		this.lifecycle = lifecycle;

		execute(monitor);
	}
	
	@Override
	public <ExecutorType> Promise execute(final JJTask<ExecutorType> task) {

		final Promise promise = task.promise().taskRunner(this);
		final TaskTracker tracker = new TaskTracker(clock, task);
		
		tracker.enqueue();
		queuedTasks.add(tracker);
		
		executors.executeTask(task, () -> {

			String oldName = Thread.currentThread().getName();
			String threadName = oldName + " - " + task.name();
			Thread.currentThread().setName(threadName);

			boolean interrupted = false;
			queuedTasks.remove(tracker);
			tracker.start();

			try (Closer closer = currentTask.enterScope(task)) {
				task.runningThread = Thread.currentThread();
				task.run();
			} catch (InterruptedException ie) {
				Thread.interrupted(); // clear the status in case the thread can get reused
				interrupted = true;
			} catch (AssertionError ae) {
				System.err.println("ASSERTION TRIPPED");
				ae.printStackTrace();
				lifecycle.stop();
			} catch (OutOfMemoryError e) {
				throw e; // just in case
			} catch (Throwable t) {
				if (!task.errored(t)) {
					publisher.publish(new Emergency("Task [" + task.name() + "] ended in exception", t));
					tracker.endedInError();
				}

			} finally {
				task.runningThread = null;
				tracker.end();

				// interruption means don't bother keeping promises
				if (!interrupted) {
					List<JJTask<?>> tasks = promise.done();
					if (tasks != null) {
						tasks.forEach(this::execute);
					}
				}

				publisher.publish(tracker);
				Thread.currentThread().setName(oldName);

			}
		});
		
		return promise;
	}
}
