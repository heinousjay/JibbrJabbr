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
	
	final URI indexHtml;
	final URI clamwhoresFS;
	final URI clamwhoresJar;
	
	
	public FileSystemServiceTest() throws Exception {
		indexHtml = FileSystemServiceTest.class.getResource("/com/clamwhores/index.html").toURI();
		clamwhoresFS = indexHtml.resolve(".");
		clamwhoresJar = FileSystemServiceTest.class.getResource("/clamwhores.jar").toURI();
	}

	@Test
	public void testUriToPathSuccess() throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(2);
		final AtomicBoolean failed = new AtomicBoolean(false);
		
		new FileSystemService.UriToPath(indexHtml) {
			
			@Override
			void path(Path path) {
				if (path == null) {
					failed.set(true);
				}
				finished();
				latch.countDown();
			}
		};
		
		String uri = String.format("jar:%s!/com/clamwhores/index.html", clamwhoresJar);
		
		new FileSystemService.UriToPath(URI.create(uri)) {
			
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
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicBoolean failed = new AtomicBoolean(false);
		
		new FileBytesRetriever(indexHtml) {
			
			@Override
			protected void bytes(ByteBuffer bytes) {
				if (bytes == null || bytes.limit() == 0) {
					failed.set(true);
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
		expectedURIsClamwhoresJar.add("/com/");
		expectedURIsClamwhoresJar.add("/com/clamwhores/");
		expectedURIsClamwhoresJar.add("/com/clamwhores/clamwhores.com.png");
		expectedURIsClamwhoresJar.add("/com/clamwhores/fragment.html");
		expectedURIsClamwhoresJar.add("/com/clamwhores/index.html");
		expectedURIsClamwhoresJar.add("/com/clamwhores/style.css");
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> failed = new AtomicReference<>(null);
		
		new DirectoryTreeRetriever(clamwhoresJar) {
			
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
		expectedURIsClamwhoresFS.add("clamwhores.com.png");
		expectedURIsClamwhoresFS.add("fragment.html");
		expectedURIsClamwhoresFS.add("index.html");
		expectedURIsClamwhoresFS.add("style/");
		expectedURIsClamwhoresFS.add("style/style.css");
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> failed = new AtomicReference<>(null);
		
		new DirectoryTreeRetriever(clamwhoresFS) {
			
			/* (non-Javadoc)
			 * @see jj.io.DirectoryTreeRetriever#directoryTree(java.util.List)
			 */
			@Override
			protected void directoryTree(List<URI> uris) {
				// TODO Auto-generated method stub
				for (URI uri : uris) {
					if (!expectedURIsClamwhoresFS.remove(clamwhoresFS.relativize(uri).toString())) {
						failed.set("retrieved " + clamwhoresFS.relativize(uri) + " from directory, not expected");
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
		expectedURIsClamwhoresFS.add("clamwhores.com.png");
		expectedURIsClamwhoresFS.add("fragment.html");
		expectedURIsClamwhoresFS.add("index.html");
		expectedURIsClamwhoresFS.add("style/");
		expectedURIsClamwhoresFS.add("style/style.css");
		
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> failed = new AtomicReference<>(null);
		
		new DirectoryTreeRetriever(indexHtml) {
			
			/* (non-Javadoc)
			 * @see jj.io.DirectoryTreeRetriever#directoryTree(java.util.List)
			 */
			@Override
			protected void directoryTree(List<URI> uris) {
				// TODO Auto-generated method stub
				for (URI uri : uris) {
					if (!expectedURIsClamwhoresFS.remove(clamwhoresFS.relativize(uri).toString())) {
						failed.set("retrieved " + clamwhoresFS.relativize(uri) + " from directory, not expected");
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
