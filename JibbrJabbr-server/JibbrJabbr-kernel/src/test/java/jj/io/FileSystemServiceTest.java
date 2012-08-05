/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.io;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jj.FileSystemServiceRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class FileSystemServiceTest  {
	
	@Rule
	public FileSystemServiceRule fssRule = new FileSystemServiceRule();
	
	final URI testFile;
	final URI testDir;
	final URI testJar;
	final URI testJarFile;
	
	public FileSystemServiceTest() throws Exception {
		testFile = FileSystemServiceTest.class.getResource("/jj/io/test/something.file").toURI();
		testDir = testFile.resolve(".");
		testJar = FileSystemServiceTest.class.getResource("/jj/io/jj_io_test.jar").toURI();
		testJarFile = URI.create(String.format("jar:%s!/jj/io/test/something.file", testJar));
	}

	@Test
	public void testUriToPathSuccess() throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(2);
		final AtomicBoolean failed = new AtomicBoolean(false);
		
		new UriToPath(testFile) {
			
			@Override
			void path(Path path) {
				if (path == null) {
					failed.set(true);
				}
				finished();
				latch.countDown();
			}
		};
		
		
		new UriToPath(testJarFile) {
			
			@Override
			void path(Path path) {
				if (path == null) {
					failed.set(true);
				} 
				finished();
				latch.countDown();
			}
		};
		
		if (!latch.await(20, SECONDS)) {
			fail("latch never tripped");
		}
		
		if (failed.get()) {
			fail("couldn't resolve a path correctly");
		}
	}
	
	@Test
	public void testFileBytesRetrieverSuccess() throws Exception {
		
		final byte[] fileBytes = "This file has something in it.".getBytes(StandardCharsets.UTF_8);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicBoolean failed = new AtomicBoolean(false);
		
		new FileBytesRetriever(testFile) {
			
			@Override
			protected void bytes(ByteBuffer bytes) {
				// should be  
				// in UTF-8 or ASCII or nearly everything really
				if (bytes == null || bytes.limit() == 0) {
					failed.set(true);
				} else {
					try {
						for (byte b : fileBytes) {
							if (bytes.get() != b) {
								failed.set(true);
							}
						}
					} catch (Exception e) {
						failed.set(true);
						e.printStackTrace();
					}
					
				}
				latch.countDown();
			}
			

			@Override
			protected void failed(Throwable t) {
				t.printStackTrace();
				failed.set(true);
				latch.countDown();
			}
		};
		
		if (!latch.await(20, SECONDS)) {
			fail("latch never tripped");
		}
		
		if (failed.get()) {
			fail("couldn't retrieve file bytes");
		}
	}
	
	@Test
	public void testFileBytesJarRetrieverSuccess() throws Exception {
		
		final byte[] fileBytes = "This file has something in it.".getBytes(StandardCharsets.UTF_8);
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicBoolean failed = new AtomicBoolean(false);
		
		new FileBytesRetriever(testJarFile) {
			
			@Override
			protected void bytes(ByteBuffer bytes) {
				// should be  
				// in UTF-8 or ASCII or nearly everything really
				if (bytes == null || bytes.limit() == 0) {
					failed.set(true);
				} else {
					try {
						for (byte b : fileBytes) {
							if (bytes.get() != b) {
								failed.set(true);
							}
						}
					} catch (Exception e) {
						failed.set(true);
						e.printStackTrace();
					}
					
				}
				latch.countDown();
			}
			

			@Override
			protected void failed(Throwable t) {
				t.printStackTrace();
				failed.set(true);
				latch.countDown();
			}
		};
		
		if (!latch.await(20, SECONDS)) {
			fail("latch never tripped");
		}
		
		if (failed.get()) {
			fail("couldn't retrieve file bytes");
		}
	}
	
	@Test
	public void testDirectoryTreeRetrieverJarSuccess() throws Exception {
		

		final HashSet<String> expectedURIsClamwhoresJar = new HashSet<>();

		expectedURIsClamwhoresJar.add("/");
		expectedURIsClamwhoresJar.add("/jj/");
		expectedURIsClamwhoresJar.add("/jj/io/");
		expectedURIsClamwhoresJar.add("/jj/io/test/");
		expectedURIsClamwhoresJar.add("/jj/io/test/nothing.file");
		expectedURIsClamwhoresJar.add("/jj/io/test/something.file");
		expectedURIsClamwhoresJar.add("/jj/io/test/level2/");
		expectedURIsClamwhoresJar.add("/jj/io/test/level2/another.file");
		expectedURIsClamwhoresJar.add("/META-INF/");
		expectedURIsClamwhoresJar.add("/META-INF/MANIFEST.MF");
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> failed = new AtomicReference<>(null);
		
		new DirectoryTreeRetriever(testJar) {
			
			/* (non-Javadoc)
			 * @see jj.io.DirectoryTreeRetriever#directoryTree(java.util.List)
			 */
			@Override
			protected void directoryTree(List<URI> uris) {
				// TODO Auto-generated method stub
				for (URI uri : uris) {
					String uriS = uri.toString();
					String part = uriS.substring(uriS.indexOf('!') + 1);
					if (!expectedURIsClamwhoresJar.remove(part)) {
						failed.set("retrieved " + part + " from directory, not expected");
					}
				}
				
				if (!expectedURIsClamwhoresJar.isEmpty()) {
					failed.set("expected to find addtional uris: " + expectedURIsClamwhoresJar);
				}
				latch.countDown();
			}

			@Override
			protected void failed(Throwable t) {
				t.printStackTrace();
				failed.set(t.toString());
				latch.countDown();
			}
			
		};
		
		if (!latch.await(20, SECONDS)) {
			fail("latch never tripped");
		}
		
		if (failed.get() != null) {
			fail(failed.get());
		}
	}
	
	@Test
	public void testDirectoryTreeRetrieverDefaultFileSystemSuccess() throws Exception {
		

		final HashSet<String> expectedURIsClamwhoresFS = new HashSet<>();

		// since we check with relativize, the starting slash is assumed
		expectedURIsClamwhoresFS.add("");
		expectedURIsClamwhoresFS.add("nothing.file");
		expectedURIsClamwhoresFS.add("something.file");
		expectedURIsClamwhoresFS.add("level2/");
		expectedURIsClamwhoresFS.add("level2/another.file");
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> failed = new AtomicReference<>(null);
		
		new DirectoryTreeRetriever(testDir) {
			
			/* (non-Javadoc)
			 * @see jj.io.DirectoryTreeRetriever#directoryTree(java.util.List)
			 */
			@Override
			protected void directoryTree(List<URI> uris) {
				// TODO Auto-generated method stub
				for (URI uri : uris) {
					if (!expectedURIsClamwhoresFS.remove(testDir.relativize(uri).toString())) {
						failed.set("retrieved " + testDir.relativize(uri) + " from directory, not expected");
					}
				}
				
				if (!expectedURIsClamwhoresFS.isEmpty()) {
					failed.set("expected to find addtional uris: " + expectedURIsClamwhoresFS);
				}
				latch.countDown();
			}

			@Override
			protected void failed(Throwable t) {
				t.printStackTrace();
				failed.set(t.toString());
				latch.countDown();
			}
			
		};
		
		if (!latch.await(20, SECONDS)) {
			fail("latch never tripped");
		}
		
		if (failed.get() != null) {
			fail(failed.get());
		}
	}
	
	@Test
	public void testDirectoryTreeRetrieverDefaultFilePathSuccess() throws Exception {
		

		final HashSet<String> expectedURIsClamwhoresFS = new HashSet<>();
		expectedURIsClamwhoresFS.add("");
		expectedURIsClamwhoresFS.add("nothing.file");
		expectedURIsClamwhoresFS.add("something.file");
		expectedURIsClamwhoresFS.add("level2/");
		expectedURIsClamwhoresFS.add("level2/another.file");
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> failed = new AtomicReference<>(null);
		
		new DirectoryTreeRetriever(testFile) {
			
			/* (non-Javadoc)
			 * @see jj.io.DirectoryTreeRetriever#directoryTree(java.util.List)
			 */
			@Override
			protected void directoryTree(List<URI> uris) {
				// TODO Auto-generated method stub
				for (URI uri : uris) {
					if (!expectedURIsClamwhoresFS.remove(testDir.relativize(uri).toString())) {
						failed.set("retrieved " + testDir.relativize(uri) + " from directory, not expected");
					}
				}
				
				if (!expectedURIsClamwhoresFS.isEmpty()) {
					failed.set("expected to find addtional uris: " + expectedURIsClamwhoresFS);
				}
				latch.countDown();
			}

			@Override
			protected void failed(Throwable t) {
				t.printStackTrace();
				failed.set(t.toString());
				latch.countDown();
			}
			
		};
		
		if (!latch.await(20, SECONDS)) {
			fail("latch never tripped");
		}
		
		if (failed.get() != null) {
			fail(failed.get());
		}
	}

}
