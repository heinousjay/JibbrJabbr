package jj.execution;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.JJServerShutdownListener;

/**
 * 
 * @author jason
 *
 */
@Singleton
class IOExecutor extends ThreadPoolExecutor implements JJServerShutdownListener {
	
	public static boolean isIOThread() {
		return flag.get() != null;
	}
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();
	
	@Inject
	public IOExecutor(
		final UncaughtExceptionHandler uncaughtExceptionHandler
	) {
		super(20, 20, 20L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), 
			new ThreadFactory() {
				
				private final AtomicInteger id = new AtomicInteger();
				
				@Override
				public Thread newThread(final Runnable r) {
					final String name = String.format(
						"JibbrJabbr File I/O Handler %d", 
						id.incrementAndGet()
					);
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
					// well.. whatever.  we're about to die bb yeah
					System.err.println("I/O executor is rejecting tasks. this is a surprise if it's not a shutdown.");
				}
			}
		);
		
		allowCoreThreadTimeOut(true);
		
	}
	
	// listen for the "CONFIGURATION LOADED!" event, and reconfigure yourself!  the only parameter that can really be controlled
	// is the maximum number of worker threads.

	@Override
	public void stop() {
		shutdown();
	}
}
