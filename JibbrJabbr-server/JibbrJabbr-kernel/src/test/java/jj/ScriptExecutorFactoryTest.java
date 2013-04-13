package jj;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.ScriptExecutorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.BDDMockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutorFactoryTest {

	@Spy MockTaskCreator taskCreator;
	ScriptExecutorFactory scriptExecutorFactory;
	
	@Before
	public void before() {
		scriptExecutorFactory = new ScriptExecutorFactory(taskCreator);
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBasicInteractions() throws Exception {
		
		String baseName = "index";
		ScheduledExecutorService index = scriptExecutorFactory.executorFor(baseName);
		
		final AtomicBoolean failed1 = new AtomicBoolean();
		final AtomicBoolean failed2 = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(2);
		final Runnable submitted = new Runnable() {
			
			@Override
			public void run() {
				failed1.set(!scriptExecutorFactory.isScriptThread());
				latch.countDown();
			}
		};
		
		index.submit(submitted);
		
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
		
		verify(taskCreator).newScriptTask(eq(submitted), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newHttpTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newClientTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newIOTask(BDDMockito.any(Runnable.class), BDDMockito.any(Object.class));
	}
	
	@SuppressWarnings("unchecked")
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
		
		verify(taskCreator, times(3)).newScriptTask(eq(testRunnable), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newHttpTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newClientTask(BDDMockito.any(Runnable.class), BDDMockito.any(RunnableScheduledFuture.class));
		verify(taskCreator, never()).newIOTask(BDDMockito.any(Runnable.class), BDDMockito.any(Object.class));
	}

}
