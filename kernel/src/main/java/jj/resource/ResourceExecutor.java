package jj.resource;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.JJRejectedExecutionHandler;
import jj.execution.JJThreadFactory;

/**
 * 
 * @author jason
 *
 */
@Singleton
@Subscriber
class ResourceExecutor extends ThreadPoolExecutor {
	
	public boolean isResourceThread() {
		return threadFactory.in();
	}
	
	private final JJThreadFactory threadFactory;
	
	@Inject
	public ResourceExecutor(
		final ResourceConfiguration configuration,
		final JJThreadFactory threadFactory,
		final JJRejectedExecutionHandler handler
	) {
		super(
			configuration.ioThreads(),
			configuration.ioThreads(),
			20, SECONDS,
			new LinkedBlockingQueue<Runnable>(), 
			threadFactory.namePattern("JibbrJabbr Resource Thread %d"), 
			handler
		);
		this.threadFactory = threadFactory;
		
		allowCoreThreadTimeOut(true); // we can respond to burst of activity and then shut down waiting
		
	}
	
	// listen for the "CONFIGURATION LOADED!" event, and reconfigure yourself!  the only parameter that can really be controlled
	// is the maximum number of worker threads, which is also the core, which we allow to die off

	@Listener
	public void stop(ServerStopping event) {
		shutdown();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
