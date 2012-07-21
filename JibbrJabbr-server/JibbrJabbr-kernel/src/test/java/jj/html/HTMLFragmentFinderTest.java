package jj.html;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jj.KernelControl;
import jj.MockLogger;
import jj.MockThreadPool;
import jj.io.FileSystemService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

public class HTMLFragmentFinderTest {

	HTMLFragmentCache htmlFragmentCache;
	FileSystemService fileSystemService;
	URI clamwhoresIndex;
	MockThreadPool testThreadPool;
	MessageConveyor messages = new MessageConveyor(Locale.US);
	MockLogger mockLogger;
	

	
	/**
	 * @throws Exception 
	 * 
	 */
	public HTMLFragmentFinderTest() throws Exception {
		clamwhoresIndex = getClass().getResource("/com/clamwhores/index.html").toURI();
	}

	@Before
	public void before() throws Exception {
		mockLogger = new MockLogger();
		testThreadPool = new MockThreadPool();
		fileSystemService = new FileSystemService(testThreadPool, new LocLogger(mockLogger, messages), messages);
		htmlFragmentCache = new HTMLFragmentCache(testThreadPool, new LocLogger(mockLogger, messages), messages);
	}
	
	@After
	public void after() {
		htmlFragmentCache.control(KernelControl.Dispose);
		fileSystemService.control(KernelControl.Dispose);
		testThreadPool.shutdown();
		testThreadPool = null;
	}
	
	@Test
	public void testBasicFragmentRetrieval() throws Exception {
		
		final CountDownLatch latch1 = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(1);
		final AtomicBoolean failed = new AtomicBoolean(false);
		final AtomicReference<HTMLFragment> fragment = new AtomicReference<>(null);
		
		new HTMLFragmentFinder(clamwhoresIndex) {
			
			@Override
			protected void htmlFragment(HTMLFragment htmlFragment) {
				if (htmlFragment == null) {
					failed.set(true);
				} else {
					fragment.compareAndSet(null, htmlFragment);
				}
				latch1.countDown();
			}
		};
		
		latch1.await(10, SECONDS);
		
		new HTMLFragmentFinder(clamwhoresIndex) {
			
			@Override
			protected void htmlFragment(HTMLFragment htmlFragment) {
				if (htmlFragment != fragment.get()) {
					failed.set(true);
				} 
				
				latch2.countDown();
			}
		};
		
		if (!latch2.await(10, SECONDS) || !failed.get()) {
			fail("something didn't work");
		}
	}
}
