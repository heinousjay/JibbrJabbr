package jj;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * You should almost certainly not depend on this class.  depend on
 * {@link JJExecutors} instead. if there is something you need exposed,
 * add it to the JJExecutors class.
 * @author jason
 *
 */
public class IOExecutor extends ThreadPoolExecutor {
	
	public boolean isIOThread() {
		return flag.get() != null;
	}
	
	private static final ThreadLocal<Boolean> flag = new ThreadLocal<>();

	// watch service eats one of these threads,
	// and four for dealing with loading
	public static final int WORKER_COUNT = 5;
	
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
				System.err.println("ran out of room for an i/o task.  OOM error coming shortly!");
			}
		};
		
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
}