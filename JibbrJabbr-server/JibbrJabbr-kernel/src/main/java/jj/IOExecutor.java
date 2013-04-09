package jj;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * You should almost certainly not depend on this class.  depend on
 * {@link JJExecutors} instead. if there is something you need exposed,
 * add it to the JJExecutors class.
 * @author jason
 *
 */
@Singleton
public class IOExecutor extends ThreadPoolExecutor implements JJServerListener {
	
	public static boolean isIOThread() {
		return flag.get() != null;
	}
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();

	// watch service eats one thread
	// plus half the processors
	public static final int WORKER_COUNT = 1 + (int)(Runtime.getRuntime().availableProcessors() * 0.5);
	
	private static final ThreadFactory threadFactory = new ThreadFactory() {
		
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
			return thread;
		}
	};
	
	private static final RejectedExecutionHandler rejectedExecutionHandler =
		new RejectedExecutionHandler() {
			
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				// well.. whatever.  we're about to die bb yeah
				System.err.println("I/O executor is rejecting tasks. this would be surprising.");
			}
		};
		
	@Inject
	public IOExecutor() {
		super(
			WORKER_COUNT, 
			WORKER_COUNT,
			0,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(),
			threadFactory,
			rejectedExecutionHandler
		);
	}

	@Override
	public void start() throws Exception {
		// nothing to do
	}

	@Override
	public void stop() {
		shutdownNow();
	}
}
