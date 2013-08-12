package jj.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerShutdownListener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ScriptExecutorFactory implements JJServerShutdownListener {
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();
	
	private final Logger log = LoggerFactory.getLogger(ScriptExecutorFactory.class);
	
	private final AtomicInteger seq = new AtomicInteger();

	private final ScheduledThreadPoolExecutor executor;
		
	@Inject
	ScriptExecutorFactory(
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		executor = new ScheduledThreadPoolExecutor(
			1,
			new ThreadFactory() {
				
				@Override
				public Thread newThread(final Runnable r) {
					
					String name = String.format("ScriptExecutor %s", seq.incrementAndGet());
					Thread thread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							flag.set(true);
							r.run();
						}
					}, name);
					thread.setDaemon(true);
					thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
					return thread;
				}
			},
			new RejectedExecutionHandler() {
				
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					log.error("a script task was rejected.  it's like a miracle only backwards.  system crash imminent.");
				}
			}
		) {
			
			{
				setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
				setRemoveOnCancelPolicy(true);
			}
			
			@Override
			protected <V> RunnableScheduledFuture<V> decorateTask(
				final Runnable runnable,
				final RunnableScheduledFuture<V> task
			) {
				return new JJScheduledTask<>(runnable, task);
			}
			
			@Override
			protected <V> RunnableScheduledFuture<V> decorateTask(
				final Callable<V> callable,
				final RunnableScheduledFuture<V> task
			) {
				System.err.println("something asked for a callable");
				new Exception().printStackTrace();
				return task;
			}
		};
	}
	
	public ScheduledExecutorService executorFor(String baseName) {
		// for now, we always just return our one single thread executor
		// later we'll make a new one for each baseName? or pool them
		// up somehow.  or something that takes good advantage of our
		// lovely modern multicore processors.  i'm starting to feel
		// like this is a great match for that Atom server thing -
		// gimme a bunch of weak cores and a hunk of ram and stand back
		return executor;
	}
	
	public boolean isScriptThreadFor(String baseName) {
		// TODO if this gets smart about handing out scripts take this into account
		return flag.get() != null;
	}

	@Override
	public void stop() {
		executor.shutdownNow();
	}

	/**
	 * @return
	 */
	public boolean isScriptThread() {
		return flag.get() != null;
	}

}
