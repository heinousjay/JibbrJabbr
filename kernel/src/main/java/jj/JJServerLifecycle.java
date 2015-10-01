package jj;

import static java.util.concurrent.TimeUnit.*;
import static jj.server.ServerLocation.Root;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private final AtomicBoolean started = new AtomicBoolean(false);

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

		if (started.compareAndSet(false, true)) {
			ServerStarting startupEvent = new ServerStarting(server.resolvePath(Root), version);
			publisher.publish(startupEvent);

			for (ServerStarting.Priority priority : ServerStarting.Priority.values()) {
				List<JJTask<?>> tasks = startupEvent.startupTasks().get(priority);
				if (tasks != null) {
					CountDownLatch latch = new CountDownLatch(tasks.size());
					for (JJTask<?> task : tasks) {
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
		} else {
			// getting double started is bad!
			new Error("being double started!").printStackTrace();
			//System.exit(1);
		}
	}

	public void stop() {
		if (started.compareAndSet(true, false)) {
			publisher.publish(new ServerStopping());
		} else {
			// getting double stooped is also bad!
			new Error("being double stopped!").printStackTrace();
			//System.exit(1);
		}
	}
}
