package jj.execution;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
	
	public boolean isIOThread() {
		return threadFactory.in();
	}
	
	private final JJThreadFactory threadFactory;
	
	@Inject
	public IOExecutor(
		final JJThreadFactory threadFactory,
		final JJRejectedExecutionHandler handler
	) {
		super(20, 20, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), 
			threadFactory.namePattern("JibbrJabbr I/O Thread %d"), 
			handler
		);
		this.threadFactory = threadFactory;
		
		allowCoreThreadTimeOut(true); // we can respond to burst of activity and then shut down waiting
		
	}
	
	// listen for the "CONFIGURATION LOADED!" event, and reconfigure yourself!  the only parameter that can really be controlled
	// is the maximum number of worker threads, which is also the core, which we allow to die off

	@Override
	public void stop() {
		shutdown();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
