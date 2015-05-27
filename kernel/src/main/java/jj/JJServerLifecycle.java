package jj;

import static java.util.concurrent.TimeUnit.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.event.Publisher;
import jj.execution.JJTask;
import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import jj.server.Server;

@Singleton
public class JJServerLifecycle {

	private final Server server;
	private final Publisher publisher;
	private final TaskRunner taskRunner;
	private final Version version;

	@Inject
	JJServerLifecycle(
		final Server server,
		final Publisher publisher,
		final TaskRunner taskRunner,
		final Version version
	) {
		this.server = server;
		this.publisher = publisher;
		this.taskRunner = taskRunner;
		this.version = version;
	}
	
	public void start() throws Exception {

		ServerStarting startupEvent = new ServerStarting(server.path(), version);
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
