package jj;

import static java.lang.annotation.ElementType.*;
import static java.util.concurrent.TimeUnit.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.execution.JJTask;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;

@Singleton
public class JJServerLifecycle {
	
	@Qualifier
	@Target(PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@interface StartupListeners {}

	private final Publisher publisher;
	private final TaskRunner taskRunner;
	private final Version version;

	@Inject
	JJServerLifecycle(
		final Publisher publisher,
		final TaskRunner taskRunner,
		final Version version
	) {
		this.publisher = publisher;
		this.taskRunner = taskRunner;
		this.version = version;
	}
	
	public void start() throws Exception {

		ServerStarting startupEvent = new ServerStarting(version);
		publisher.publish(startupEvent);

		for (ServerStarting.Priority priority : ServerStarting.Priority.values()) {
			List<JJTask> tasks = startupEvent.startupTasks().get(priority);
			if (tasks != null) {
				CountDownLatch latch = new CountDownLatch(tasks.size());
				for (JJTask task : tasks) {
					taskRunner.execute(task).then(new ServerTask("counting down " + priority + " priority startup tasks") {
						@Override
						protected void run() throws Exception {
							latch.countDown();
						}
					});
				}
				
				latch.await(1, SECONDS);
			}
		}
	}

	public void stop() {
		publisher.publish(new ServerStopping());
	}
}
