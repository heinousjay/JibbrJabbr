package jj.script;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ScriptExecutorFactory {
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();
	
	private final Logger log = LoggerFactory.getLogger(ScriptExecutorFactory.class);
	
	private final AtomicInteger seq = new AtomicInteger();
	
	private final ThreadFactory threadFactory =
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
				return thread;
			}
		};
		
	private final RejectedExecutionHandler rejectedExecutionHandler =
		new RejectedExecutionHandler() {
			
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				log.error("a script task was rejected.  it's like a miracle only backwards.  system crash imminent.");
			}
		};
		
	private final ScheduledThreadPoolExecutor executor;
		
	@Inject
	ScriptExecutorFactory(
	) {
		executor = new ScheduledThreadPoolExecutor(1, threadFactory, rejectedExecutionHandler);
		executor.setMaximumPoolSize(1);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		executor.setRemoveOnCancelPolicy(true);
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
	
	public boolean isScriptThread() {
		return flag.get() != null;
	}

}
