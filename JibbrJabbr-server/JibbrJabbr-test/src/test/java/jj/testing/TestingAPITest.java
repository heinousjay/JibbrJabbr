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
package jj.testing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static jj.resource.AssetResource.*;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import jj.JJ;
import jj.resource.TestAssetResourceCreator;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Started as a scratchpad for a testing API, survives as a basic integration/stress test
 * 
 * @author jason
 *
 */
public class TestingAPITest {
	
	public static class VerifiableRequest {
		
		public final String uri;
		public final byte[] bytes;
		
		public VerifiableRequest(final String uri, final byte[] bytes) {
			this.uri = uri;
			this.bytes = bytes;
		}
	}
	
	/**
	 * 
	 */
	private static final String INDEX_HTML_RENDERED = "index.html.rendered";
	private static final String ANIMAL_HTML_RENDERED = "animal.html.rendered";

	private static final String INDEX = "/index";

	private static final String ANIMAL = "/animal";
	static final String INDEX_TITLE = "API TEST SUCCESS";
	static final String ANIMAL_TITLE = "ANIMAL!";
	
	static final String basePath;
	private static final Path indexHtmlRenderedPath;
	private static final Path animalHtmlRenderedPath;
	
	public static final VerifiableRequest[] statics;
	public static final VerifiableRequest[] documents;
	public static final VerifiableRequest[] stylesheets;
	public static final VerifiableRequest[] assets;
	
	private static VerifiableRequest makeResourceRequest(String name) throws Exception {
		return new VerifiableRequest("/" + name, Files.readAllBytes(Paths.get(basePath, name)));
	}
	
	static {
		try {
			// well it's ugly, but it's portable
			basePath = Paths.get(JJ.uri(TestingAPITest.class)).getParent().getParent().getParent().toAbsolutePath().toString();
			indexHtmlRenderedPath = Paths.get(basePath, INDEX_HTML_RENDERED);
			animalHtmlRenderedPath = Paths.get(basePath, ANIMAL_HTML_RENDERED);
			
			List<VerifiableRequest> work = new ArrayList<>();
			work.add(makeResourceRequest("0.txt"));
			work.add(makeResourceRequest("1.txt"));
			work.add(makeResourceRequest("2.txt"));
			work.add(makeResourceRequest("3.txt"));
			work.add(makeResourceRequest("4.txt"));
			work.add(makeResourceRequest("5.txt"));
			work.add(makeResourceRequest("6.txt"));
			work.add(makeResourceRequest("7.txt"));
			work.add(makeResourceRequest("8.txt"));
			work.add(makeResourceRequest("9.txt"));
		
			statics = work.toArray(new VerifiableRequest[work.size()]);
			
			work = new ArrayList<>();
			work.add(new VerifiableRequest(INDEX, Files.readAllBytes(indexHtmlRenderedPath)));
			work.add(new VerifiableRequest(ANIMAL, Files.readAllBytes(animalHtmlRenderedPath)));
			
			documents = work.toArray(new VerifiableRequest[work.size()]);
			
			work = new ArrayList<>();
			work.add(makeResourceRequest("test.css"));
			work.add(makeResourceRequest("style.css"));
			
			stylesheets = work.toArray(new VerifiableRequest[work.size()]);
			
			TestAssetResourceCreator assetCreator = new TestAssetResourceCreator();
			work = new ArrayList<>();
			work.add(new VerifiableRequest("/" + JQUERY_JS, assetCreator.toBytes(JQUERY_JS)));
			work.add(new VerifiableRequest("/" + JJ_JS, assetCreator.toBytes(JJ_JS)));
			work.add(new VerifiableRequest("/" + FAVICON_ICO, assetCreator.toBytes(FAVICON_ICO)));
			
			assets = work.toArray(new VerifiableRequest[work.size()]);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("failed!", e);
		}
	}
	
	@Rule
	public JJAppTest app = new JJAppTest(basePath);
	
	static interface Namer {
		String name(int i);
		Path path(int i);
	}
	
	static interface RequestMaker {
		VerifiableRequest make();
	}
	
	private void runStressTestPattern(final int totalClients, final RequestMaker maker) throws Exception {
		for (int i = 0; i < totalClients; ++i) {
			final VerifiableRequest request = maker.make();
			final TestHttpClient client = app.get(request.uri);
			try {
				
				assertThat(client.status(), is(HttpResponseStatus.OK));
				assertThat(request.uri, client.contentBytes(), is(request.bytes));
			} catch (Exception e) {
				System.err.print(new StringBuilder()
					.append(request.uri).append(System.lineSeparator())
				);
				e.printStackTrace();
				throw new AssertionError("stress test failed");
			}
		}
		
	}
	
	@Ignore
	@Test
	public void writeDocumentBytes_notATest() throws Exception {
		TestHttpClient client = app.get(ANIMAL);
		
		byte[] bytes = client.contentBytes();
		String string = client.contentsString();
		System.out.println(string);
		assertThat(string, is(new String(bytes, UTF_8)));
		
		Files.write(animalHtmlRenderedPath, bytes);
	}
	
	private void runBasicStressTest(final int total , final VerifiableRequest[] toRun) throws Exception {
		runStressTestPattern(total, new RequestMaker() {
			
			private AtomicInteger count = new AtomicInteger(total);
			
			@Override
			public VerifiableRequest make() {
				return toRun[count.getAndIncrement() % toRun.length];
			}
		});
	}
	
	@Test
	public void runDocumentTest() throws Exception {
		runBasicStressTest(400, documents);
	}
	
	@Test
	public void runStaticTest() throws Exception {
		runBasicStressTest(400, statics);
	}
	
	@Test
	public void runCssTest() throws Exception {
		runBasicStressTest(400, stylesheets);
	}
	
	@Test
	public void runAssetTest() throws Exception {
		runBasicStressTest(400, assets);
	}
	
	private VerifiableRequest[] makeAll() {
		List<VerifiableRequest> requests = new ArrayList<>();
		Collections.addAll(requests, documents);
		Collections.addAll(requests, statics);
		Collections.addAll(requests, stylesheets);
		Collections.addAll(requests, assets);
		return requests.toArray(new VerifiableRequest[requests.size()]);
	}
	
	@Test
	public void runMixedTest() throws Exception {
		runBasicStressTest(1600, makeAll());
	}
	
	@Ignore
	@Test
	public void poundIt() throws Throwable {
		timePoundIt(12, 4000);
	}
	
	public void poundIt(final int threadCount, final int perClientRequestCount) throws Exception {
		final VerifiableRequest[] requests = makeAll();
		final CountDownLatch latch = new CountDownLatch(threadCount);
		final ExecutorService service = Executors.newFixedThreadPool(threadCount);
	
		final Throwable[] throwables = new Throwable[threadCount];
		try {
			for (int i = 0; i < threadCount; ++i) {
				final int index = i;
				service.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							runBasicStressTest(perClientRequestCount, requests);
						} catch (Throwable t) {
							throwables[index] = t;
						} finally {
							latch.countDown();
						}
					}
				});
			}
			
			
			latch.await();
			
		} finally {
			service.shutdownNow();
		}
		boolean failed = false;
		for (Throwable t : throwables) {
			if (t != null) {
				failed = true;
				t.printStackTrace();
			}
		}
		
		if (failed) fail("FAILED");
	}
	
	private void timePoundIt(final int threadCount, final int perClientRequestCount) throws Throwable {
		final long startingTotalMemory = Runtime.getRuntime().totalMemory();
		final long startingMaxMemory = Runtime.getRuntime().maxMemory();
		final long startingFreeMemory = Runtime.getRuntime().freeMemory();
		final long start = System.currentTimeMillis();
		
		poundIt(threadCount, perClientRequestCount);
		
		int total = (perClientRequestCount * threadCount);
		long time = (System.currentTimeMillis() - start);
		
		System.out.println("loaded " + total + " responses in " + time + " milliseconds.");
		System.out.println("free\t" + startingFreeMemory + "\ttotal\t" + startingTotalMemory + "\tmax\t" + startingMaxMemory + " at start");
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory() + " at finish");
		Runtime.getRuntime().gc();
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory() + " after GC");
	
	}
	
	@Ignore // until there is a cheaper method of comparison with expected responses.
	// the current comparison is biasing the result
	@Test
	public void areYouKiddingMePart2() throws Throwable {
		final int threadCount = 12;
		// one quiet one to warm things up
		poundIt(threadCount, 4000);
		// and now make them loud
		timePoundIt(threadCount, 5000);
		System.out.println();
		timePoundIt(threadCount, 500);
		System.out.println();
		timePoundIt(threadCount, 5000);
	}
}
