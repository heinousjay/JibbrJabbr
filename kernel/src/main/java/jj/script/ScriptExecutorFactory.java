package jj.script;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.JJRejectedExecutionHandler;
import jj.execution.JJThreadFactory;

@Singleton
@Subscriber
class ScriptExecutorFactory {
	
	private final class ScriptExecutor extends ScheduledThreadPoolExecutor {
		
		ScriptExecutor(
			final JJThreadFactory threadFactory,
			final JJRejectedExecutionHandler handler
		) {
			super(1, threadFactory, handler);
			setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			setRemoveOnCancelPolicy(true);
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}
	
	private final ScheduledExecutorService executor;
	
	private final JJThreadFactory threadFactory;
		
	@Inject
	ScriptExecutorFactory(
		final JJThreadFactory threadFactory,
		final JJRejectedExecutionHandler handler
	) {
		this.executor = new ScriptExecutor(threadFactory.namePattern("JibbrJabbr Script Thread %s"), handler);
		this.threadFactory = threadFactory;
	}
	
	ScheduledExecutorService executorFor(ScriptEnvironment scriptEnvironment) {
		// remember! when you fix this so there are pools of script threads, you also
		// have to ensure that it's the root script environment that is in charge of things
		return executor;
	}
	
	boolean isScriptThreadFor(ScriptEnvironment scriptEnvironment) {
		return threadFactory.in();
	}

	@Listener
	public void stop(ServerStopping event) {
		executor.shutdownNow();
	}

	/**
	 * @return
	 */
	public boolean isScriptThread() {
		return threadFactory.in();
	}

}
