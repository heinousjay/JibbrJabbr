package jj.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.RejectedExecutionHandler;
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
	
	private static final String SPEC_EXECUTOR_NAME = "ScriptExecutor for specs";

	private final ThreadLocal<String> flag = new ThreadLocal<>();
	
	private final Logger log = LoggerFactory.getLogger(ScriptExecutorFactory.class);
	
	private final AtomicInteger seq = new AtomicInteger();
	
	private final UncaughtExceptionHandler uncaughtExceptionHandler;
	
	private final class InnerBridge implements ThreadFactory, RejectedExecutionHandler {
		
		private final String name;
		
		InnerBridge() {
			name = String.format("ScriptExecutor %s", seq.incrementAndGet());
		}
		
		InnerBridge(final String name) {
			this.name = name;
			seq.incrementAndGet();
		}
		
		@Override
		public Thread newThread(final Runnable r) {
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					flag.set(name);
					r.run();
				}
			}, name);
			thread.setDaemon(true);
			thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
			return thread;
		}
		
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			log.error("a script task was rejected.  it's like a miracle only backwards.  system crash imminent.");
		}
	}
	
	private final class ScriptExecutor extends ScheduledThreadPoolExecutor {
		
		ScriptExecutor(InnerBridge innerBridge) {
			super(1, innerBridge, innerBridge);
			setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			setRemoveOnCancelPolicy(true);
		}
	}
	
	private final ScheduledExecutorService executor;
	
	private final ScheduledExecutorService specExecutor;
		
	@Inject
	ScriptExecutorFactory(
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
		this.executor = new ScriptExecutor(new InnerBridge());
		this.specExecutor = new ScriptExecutor(new InnerBridge(SPEC_EXECUTOR_NAME));
	}
	
	public ScheduledExecutorService executorFor(String baseName) {
		return executor;
	}
	
	public ScheduledExecutorService specExecutor() {
		return specExecutor;
	}
	
	public boolean isScriptThreadFor(String baseName) {
		return flag.get() != null && !SPEC_EXECUTOR_NAME.equals(flag.get());
	}
	
	public boolean isSpecExecutor() {
		return SPEC_EXECUTOR_NAME.equals(flag.get());
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
