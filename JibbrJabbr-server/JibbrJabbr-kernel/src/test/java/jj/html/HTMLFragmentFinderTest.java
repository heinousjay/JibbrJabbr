package jj.html;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import jj.JJ;
import jj.NettyRequestBridge;
import jj.SynchronousOperationCallback;
import jj.TestThreadPool;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HTMLFragmentFinderTest {

	private static final String FRAGMENT_HTML = "fragment.html";
	private static final String INDEX_HTML = "index.html";
	HTMLFragmentFinder htmlFragmentFinder;
	Path clamwhoresBase;
	TestThreadPool testThreadPool;
	
	@Before
	public void before() throws Exception {
		testThreadPool = new TestThreadPool();
		htmlFragmentFinder = new HTMLFragmentFinder(testThreadPool, testThreadPool);
		if (clamwhoresBase == null) {
			Path clamwhoresIndex = Paths.get(getClass().getResource("/com/clamwhores/index.html").toURI());
			clamwhoresBase = clamwhoresIndex.getParent();
		}
	}
	
	@After
	public void after() {
		htmlFragmentFinder = null;
		testThreadPool.shutdownNow();
		testThreadPool = null;
	}
	
	@Test
	public void testFindArgumentErrors() {
		Path nullPath = null;
		String nullString = null;
		SynchronousOperationCallback<HTMLFragment> nullCallback = null;
		boolean failed = false;
		try {
			htmlFragmentFinder.find(nullPath, nullString);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("base"));
		}
		if (failed) fail("should have thrown AssertionError");
		
		try {
			htmlFragmentFinder.find(clamwhoresBase, nullString);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("url"));
		}
		if (failed) fail("should have thrown AssertionError");
		
		try {
			htmlFragmentFinder.find(nullPath, INDEX_HTML);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("base"));
		}
		if (failed) fail("should have thrown AssertionError");
		
		try {
			htmlFragmentFinder.find(nullPath, nullString, nullCallback);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("base"));
		}
		if (failed) fail("should have thrown AssertionError");
		
		try {
			htmlFragmentFinder.find(clamwhoresBase, nullString, nullCallback);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("url"));
		}
		if (failed) fail("should have thrown AssertionError");
		
		try {
			htmlFragmentFinder.find(nullPath, INDEX_HTML, nullCallback);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("base"));
		}
		if (failed) fail("should have thrown AssertionError");
		
		try {
			htmlFragmentFinder.find(clamwhoresBase, INDEX_HTML, nullCallback);
			failed = true;
		} catch (AssertionError iae) {
			assertThat(iae.getMessage(), containsString("callback"));
		}
		if (failed) fail("should have thrown AssertionError");
	}
	
	@Test
	public void testReturnsNullForUnknownResource() {
		assertThat(htmlFragmentFinder.find(clamwhoresBase, "nonsense"), is(nullValue()));
	}
	
	@Test
	public void testFindsHTMLFragment() {
		
		HTMLFragment index = htmlFragmentFinder.find(clamwhoresBase, INDEX_HTML);
		assertThat(index, is(notNullValue()));
		assertThat(index.element(), is(instanceOf(Document.class)));
		
		HTMLFragment fragment = htmlFragmentFinder.find(clamwhoresBase, FRAGMENT_HTML);
		assertThat(fragment, is(notNullValue()));
		assertThat(fragment.element(), is(not(instanceOf(Document.class))));
	}
	
	@Test 
	public void testCachesHTMLFragments() {
		HTMLFragment index1 = htmlFragmentFinder.find(clamwhoresBase, INDEX_HTML);
		HTMLFragment index2 = htmlFragmentFinder.find(clamwhoresBase, INDEX_HTML);
		HTMLFragment fragment1 = htmlFragmentFinder.find(clamwhoresBase, FRAGMENT_HTML);
		HTMLFragment fragment2 = htmlFragmentFinder.find(clamwhoresBase, FRAGMENT_HTML);
		assertThat(index1, is(sameInstance(index2)));
		assertThat(fragment1, is(sameInstance(fragment2)));
	}
	
	@Test
	public void testFindsFragmentsAsynchronously() throws Exception {
		
		final int count = 20;
		
		final CountDownLatch latch = new CountDownLatch(count);
		final AtomicBoolean failed = new AtomicBoolean(false);
		
		try (FileSystem jarFS = FileSystems.newFileSystem(Paths.get(getClass().getResource("/clamwhores.jar").toURI()), null)) {
			final Runnable runner = new Runnable() {
				
				@Override
				public void run() {
					htmlFragmentFinder.find(jarFS.getPath("jj"), "/com/clamwhores/index.html", new SynchronousOperationCallback<HTMLFragment>() {

						@Override
						public void complete(HTMLFragment htmlFragment) {
							assertThat(htmlFragment, is(notNullValue()));
							assertThat(htmlFragment.element(), is(instanceOf(Document.class)));
							latch.countDown();
						}
						
						@Override
						public void throwable(Throwable t) {
							t.printStackTrace();
							latch.countDown();
							failed.set(true);
						}
					});
				}
			};
			
			for (int i = 0; i < count; ++i) {
				testThreadPool.submit(runner);
			}
			
			try {
				latch.await(30, SECONDS);
			} catch (Exception eaten) { failed.set(true); }
			
			if (failed.get()) {
				fail("something didn't work");
			}
		}
	}
}
