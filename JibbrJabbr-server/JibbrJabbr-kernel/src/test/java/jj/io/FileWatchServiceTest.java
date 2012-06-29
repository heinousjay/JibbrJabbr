package jj.io;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;


import jj.MockLogger;
import jj.MockLogger.LogBundle;
import jj.MockThreadPool;

import jj.io.FileWatchService;
import jj.io.FileWatchSubscription;

import org.jboss.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.cal10n.LocLogger;

import ch.qos.cal10n.MessageConveyor;

public class FileWatchServiceTest {

	MockLogger executorLogger = new MockLogger();
	FileWatchService underTest;
	MockThreadPool testThreadPool;
	
	@Before
	public void before() throws Exception {
		// so much set-up
		MessageConveyor messageConveyor = new MessageConveyor(Locale.US);
		LocLogger logger = new LocLogger(executorLogger, messageConveyor);
		testThreadPool = new MockThreadPool();
		underTest = new FileWatchService(
			testThreadPool,
			testThreadPool,
			logger,
			messageConveyor
		);
	}
	
	@After
	public void after() {
		underTest = null;
		testThreadPool.shutdownNow();
		testThreadPool = null;
		
		for (LogBundle lb : executorLogger.messages()) {
			System.out.println(lb);
		}
	}
	
	private SimpleFileVisitor<Path> treeDeleter = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
            if (e == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                // directory iteration failed
                throw e;
            }
        }
    };
    
    private void deleteTree(Path path) {
    	try {
			Files.walkFileTree(path, treeDeleter);
		} catch (Exception e) {
			System.err.println("Trouble deleting " + path);
		}
    }
    
    @Test
    public void testNullArgumentsAreRejected() throws Exception {
  
    	try {
    		new FileWatchSubscription(null) {
    			@Override
    			protected void fileChanged(Path path, Kind<Path> kind) {
    				// doesn't matter
    			}
    		};
    		fail("should not accept nulls");
    	} catch (AssertionError err) {}

    }
	
	@Test
	public void testBasicListening() throws Exception {
		final Path baseDirectory = Files.createTempDirectory(getClass().getSimpleName());
		final Path file = Files.createFile(baseDirectory.resolve("test.txt"));
		final CyclicBarrier gate = new CyclicBarrier(2, new Runnable() {
			@Override
			public void run() {
				System.out.println("testBasicListening gate tripped");
			}
		});
		final AtomicBoolean testFailedInCallback = new AtomicBoolean(false);

		try {
			new FileWatchSubscription(file) {

				@Override
				protected void fileChanged(Path path, Kind<Path> kind) {
					System.out.println(path);
					try {
						gate.await();
					} catch (InterruptedException | BrokenBarrierException e) {
						testFailedInCallback.set(true);
					}
				}
				
			};
			
			new FileWatchSubscription(baseDirectory) {

				@Override
				protected void fileChanged(Path path, Kind<Path> kind) {
					System.out.println("watching " + baseDirectory);
					System.out.println("received " + path + " " + kind);
					try {
						Files.write(
							file, 
							String.format("created %s\n", path).getBytes(CharsetUtil.UTF_8)
						);
					} catch (IOException e) {
						testFailedInCallback.set(true);
					}
				}
			};
			
			Thread.sleep(1500); // need to give it a moment to wake up
			
			Files.createDirectory(baseDirectory.resolve("HI"));
			try { 
				// we need to wait a while because
				// there are still some polling implementations of
				// the listener and changes aren't picked up for ~10 seconds
				gate.await(30, SECONDS); 
			} catch (TimeoutException te) {
				fail("timed out at the gate");
			}
			if (testFailedInCallback.get()) {
				fail("Couldn't write the test file");
			}
		} finally {
			deleteTree(baseDirectory);
		}
	}
}
