package jj.execution;

import io.netty.util.concurrent.DefaultPromise;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;

import jj.logging.EmergencyLogger;

/**
 * exposes some execution related information and
 * provides the API for execution services in the
 * system.  DO NOT DEPEND ON THIS DIRECTLY, use the
 * interface
 * 
 * @author jason
 *
 */
@Singleton
class JJExecutorsImpl implements JJExecutors {

	private final ExecutorBundle bundle;
	private final Logger logger;
	
	@Inject
	public JJExecutorsImpl(
		final ExecutorBundle bundle,
		final @EmergencyLogger Logger logger
	) {
		this.bundle = bundle;
		this.logger = logger;
	}
	
	@Override
	public Future<Void> execute(final JJTask task) {
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		task.executor(bundle).submit(new Runnable() {
			
			@Override
			public void run() {
				// execution event
				try {
					task.run();
				} catch (OutOfMemoryError error) {
					throw error;
				} catch (Throwable t) {
					logger.error("Exception caught in executor", t);
				} finally {
					latch.countDown();
				}
			}
		});
		
		return new DefaultPromise<Void>() {
			
			@Override
			public boolean isDone() {
				return latch.getCount() == 0;
			}
		};
	}
	
	@Override
	public boolean isScriptThread() {
		return bundle.scriptExecutorFactory.isScriptThread();
	}
	
	@Override
	public boolean isScriptThreadFor(String baseName) {
		return bundle.scriptExecutorFactory.isScriptThreadFor(baseName);
	}

	public boolean isIOThread() {
		return IOExecutor.isIOThread();
	}
	
	// TODO replace the use of this with configuration
	public int ioPoolSize() {
		return bundle.ioExecutor.getMaximumPoolSize();
	}
}
