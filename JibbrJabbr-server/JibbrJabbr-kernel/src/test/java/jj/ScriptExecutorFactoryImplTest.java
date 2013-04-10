package jj;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.ScriptExecutorFactory;

import org.junit.Before;
import org.junit.Test;

public class ScriptExecutorFactoryImplTest {

	ScriptExecutorFactory scriptExecutorFactory;
	
	@Before
	public void before() {
		scriptExecutorFactory = new ScriptExecutorFactory();
	}
	
	@Test
	public void testSameBaseNameReturnsSameExecutor() {
		
		// need to verify that the same executor is returned for a given baseName
		
		String baseName = "index";
		ScheduledExecutorService index1 = scriptExecutorFactory.executorFor(baseName);
		ScheduledExecutorService index2 = scriptExecutorFactory.executorFor(baseName);
		
		assertThat(index1, is(index2));
		
		String baseName2 = "other";
		ScheduledExecutorService other1 = scriptExecutorFactory.executorFor(baseName2);
		ScheduledExecutorService other2 = scriptExecutorFactory.executorFor(baseName2);
		
		assertThat(other1, is(other2));
	}
	
	@Test
	public void testIsScriptThreadWorks() throws Exception {
		
		String baseName = "index";
		ScheduledExecutorService index = scriptExecutorFactory.executorFor(baseName);
		
		final AtomicBoolean failed1 = new AtomicBoolean();
		final AtomicBoolean failed2 = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(2);
		
		index.submit(new Runnable() {
			
			@Override
			public void run() {
				failed1.set(!scriptExecutorFactory.isScriptThread());
				latch.countDown();
			}
		});
		
		Executors.newSingleThreadExecutor().submit(new Runnable() {
			
			@Override
			public void run() {
				failed2.set(scriptExecutorFactory.isScriptThread());
				latch.countDown();
			}
		});
		
		latch.await(2, SECONDS);
		
		if (failed1.get()) {
			fail("script thread is not properly identified as script thread");
		}
		
		if (failed2.get()) {
			fail("non-script thread is improperly identified as script thread");
		}
	}
	
	@Test
	public void testExecutorIsSingleThreaded() throws Exception {
		
		String baseName = "index";
		ScheduledExecutorService index = scriptExecutorFactory.executorFor(baseName);
		
		final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
		final AtomicBoolean isRunning = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(3);
		final Runnable testRunnable = new Runnable() {
			
			@Override
			public void run() {
				if (isRunning.getAndSet(true)) {
					errors.add("script threads are executing concurrently!");
				} else {
					// just need to sleep long enough that another thread
					// would have started?  maybe there's something better
					// but i don't know what
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// won't happen
					}

					if (!isRunning.compareAndSet(true, false)) {
						errors.add("script threads are executing concurrently!");
					}
				}
				
				latch.countDown();
			}
		};
		
		index.submit(testRunnable);
		index.submit(testRunnable);
		index.submit(testRunnable);
		
		latch.await(2, SECONDS);
		
		if (!errors.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (String error: errors) {
				sb.append(error).append(String.format("%n"));
			}
			fail(sb.toString());
		}
	}

}
