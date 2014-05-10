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
import static jj.configuration.Assets.*;
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

import jj.App;
import jj.configuration.TestableAssets;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Started as a scratchpad for a testing API, survives as a basic integration/stress test
 * 
 * @author jason
 *
 */
public class BasicServingTest {
	
	/**
	 * 
	 */
	private static final String INDEX_HTML_RENDERED = "/app1/app/index.html.rendered";
	private static final String ANIMAL_HTML_RENDERED = "/app1/app/animal.html.rendered";

	private static final String INDEX = "/";

	private static final String ANIMAL = "/animal";
	
	public static final VerifiableRequest[] statics;
	public static final VerifiableRequest[] documents;
	public static final VerifiableRequest[] stylesheets;
	public static final VerifiableRequest[] assets;
	
	private static VerifiableRequest makeResourceRequest(String name) throws Exception {
		return new VerifiableRequest("/" + name, Files.readAllBytes(Paths.get(App.one, name)));
	}
	
	static {
		try {
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
			Collections.shuffle(work);
			statics = work.toArray(new VerifiableRequest[work.size()]);
			
			ResourceReader reader = new ResourceReader();
			
			work = new ArrayList<>();
			work.add(new VerifiableRequest(INDEX, reader.readResource(INDEX_HTML_RENDERED)));
			work.add(new VerifiableRequest(ANIMAL, reader.readResource(ANIMAL_HTML_RENDERED)));
			
			documents = work.toArray(new VerifiableRequest[work.size()]);
			
			work = new ArrayList<>();
			work.add(makeResourceRequest("test.css"));
			work.add(makeResourceRequest("style.css"));
			
			stylesheets = work.toArray(new VerifiableRequest[work.size()]);
			
			TestableAssets testAssets = new TestableAssets();

			work = new ArrayList<>();
			work.add(new VerifiableRequest("/" + JQUERY_JS, Files.readAllBytes(testAssets.path(JQUERY_JS))));
			work.add(new VerifiableRequest("/" + JJ_JS, Files.readAllBytes(testAssets.path(JJ_JS))));
			work.add(new VerifiableRequest("/" + FAVICON_ICO, Files.readAllBytes(testAssets.path(FAVICON_ICO))));
			
			assets = work.toArray(new VerifiableRequest[work.size()]);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("failed!", e);
		}
	}
	
	@Rule
	public JibbrJabbrTestServer app = new JibbrJabbrTestServer(App.one);
	
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
				client.requestAllDependencies();
				assertThat(client.status(), is(HttpResponseStatus.OK));
				assertThat(new String(client.contentBytes(), UTF_8), is(new String(request.bytes, UTF_8)));
			} catch (Error e) {
				System.err.print(new StringBuilder()
					.append(request.uri).append(System.lineSeparator())
				);
				e.printStackTrace();
				throw new AssertionError("stress test failed");
			}
		}
		
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
		Collections.shuffle(requests);
		return requests.toArray(new VerifiableRequest[requests.size()]);
	}
	
	@Test
	public void runMixedTest() throws Exception {
		timePoundIt(16, 100);
	}
	
	
	@Ignore // until there is a cheaper method of comparison with expected responses.
	// the current comparison is biasing the result
	@Test
	public void areYouKiddingMe() throws Exception {
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
	
	private void poundIt(final int threadCount, final int perClientRequestCount) throws Exception {
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
		List<String> errors = new ArrayList<>();
		for (Throwable t : throwables) {
			if (t != null) {
				errors.add(t.getMessage());
			} else {
				errors.add("fuck you");
			}
		}
		
		if (!errors.isEmpty()) fail("pounded it into submission", errors);
	}
	
	private void fail(String lead, List<String> errors) {
		StringBuilder sb = new StringBuilder(lead);
		for (String error : errors) {
			sb.append("\n").append(error);
		}
		
		throw new AssertionError(sb.toString());
	}
	
	private void timePoundIt(final int threadCount, final int perClientRequestCount) throws Exception {
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
}
