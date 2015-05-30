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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static jj.server.Assets.*;
import static jj.document.DocumentScriptEnvironment.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import jj.App;
import jj.ServerRoot;
import jj.http.server.EmbeddedHttpRequest;
import jj.http.server.EmbeddedHttpResponse;
import jj.http.server.EmbeddedHttpServer;
import jj.http.server.EmbeddedHttpServer.ResponseReady;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Started as a scratchpad for a testing API, survives as a basic integration/stress test. If something is
 * broken, this test usually quits
 * 
 * @author jason
 *
 */
public class BasicServingTest {

	private static final String INDEX = "/index";

	private static final String ANIMAL = "/animal";
	
	public static final List<String> statics;
	public static final List<String> documents;
	public static final List<String> stylesheets;
	public static final List<String> assets;
	
	static {
		List<String> work = Arrays.asList(
			"/0.txt", "/1.txt", "/2.txt",
			"/3.txt", "/4.txt", "/5.txt",
			"/6.txt", "/7.txt", "/8.txt",
			"/9.txt"
		);
		Collections.shuffle(work);
		statics = Collections.unmodifiableList(work);
		
		documents = Collections.unmodifiableList(Arrays.asList(INDEX, ANIMAL));
		
		stylesheets = Collections.unmodifiableList(Arrays.asList("/test.css", "/style.css"));
		
		assets = Collections.unmodifiableList(Arrays.asList("/" + JQUERY_JS, "/" + JJ_JS, "/" + FAVICON_ICO));
	}
	
	@Rule
	public JibbrJabbrTestServer app = 
		new JibbrJabbrTestServer(ServerRoot.one, App.one)
		.verifying()
		//.recording()
		.injectInstance(this);
	
	@Inject EmbeddedHttpServer server;
	
	@Inject TraceModeSwitch trace;
	
	private void runBasicStressTest(final int timeout, final int totalClients, final List<String> uris) throws Throwable {
		final AssertionError error = new AssertionError("failure!");
		final CountDownLatch latch = new CountDownLatch(totalClients);
		for (int i = 0; i < totalClients; ++i) {
			String uri = uris.get(i % uris.size());
			server.request(new EmbeddedHttpRequest(uri), new ResponseReady() {
				
				@Override
				public void ready(EmbeddedHttpResponse response) throws Exception {
					try {
						trace.mode().traceEvent(uri, response.bodyContentAsBytes());
					} catch (Throwable t) {
						error.addSuppressed(t);
					} finally {
						latch.countDown();
					}
				}
			});
		}
		
		boolean succeeded = latch.await(timeout, SECONDS);
		
		if (error.getSuppressed().length > 0) {
			throw error;
		}
		if (!succeeded) {
			throw new AssertionError("timed out");
		}
	}
	
	//@Test
	public void runDocumentTest() throws Throwable {
		runBasicStressTest(2, 40, documents);
	}
	
	//@Test
	public void runStaticTest() throws Throwable {
		runBasicStressTest(2, 400, statics);
	}
	
	//@Test
	public void runCssTest() throws Throwable {
		runBasicStressTest(2, 400, stylesheets);
	}
	
	//@Test
	public void runAssetTest() throws Throwable {
		runBasicStressTest(3, 400, assets);
	}
	
	private List<String> makeAll() {
		List<String> requests = new ArrayList<>();
		requests.addAll(documents);
		requests.addAll(statics);
		requests.addAll(stylesheets);
		requests.addAll(assets);
		Collections.shuffle(requests);
		return requests;
	}
	
	@Test
	public void runMixedTest() throws Exception {
		timePoundIt(16, 5, 100);
	}
	
	
	@Ignore // until there is a cheaper method of comparison with expected responses.
	// the current comparison is biasing the result
	//@Test
	public void areYouKiddingMe() throws Exception {
		final int threadCount = 12;
		// one quiet one to warm things up
		poundIt(threadCount, 5, 4000);
		// and now make them loud
		timePoundIt(threadCount, 5, 5000);
		System.out.println();
		timePoundIt(threadCount, 5, 500);
		System.out.println();
		timePoundIt(threadCount, 5, 5000);
	}
	
	private void poundIt(final int threadCount, final int perClientTimeout, final int perClientRequestCount) throws Exception {
		final List<String> requests = makeAll();
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
							runBasicStressTest(perClientTimeout, perClientRequestCount, requests);
						} catch (Throwable t) {
							throwables[index] = t;
						} finally {
							latch.countDown();
						}
					}
				});
			}
			
			
			latch.await(1, MINUTES);
			
		} finally {
			service.shutdownNow();
		}
		boolean fail = false;
		AssertionError error = new AssertionError("pounded it into submission");
		for (Throwable t : throwables) {
			if (t != null) {
				error.addSuppressed(t);
				fail = true;
			}
		}
		
		if (fail) throw error;
	}
	
	
	
	private void timePoundIt(final int threadCount, final int perClientTimeout, final int perClientRequestCount) throws Exception {
		final long startingTotalMemory = Runtime.getRuntime().totalMemory();
		final long startingMaxMemory = Runtime.getRuntime().maxMemory();
		final long startingFreeMemory = Runtime.getRuntime().freeMemory();
		final long start = System.currentTimeMillis();
		
		poundIt(perClientTimeout, threadCount, perClientRequestCount);
		
		int total = (perClientRequestCount * threadCount);
		long time = (System.currentTimeMillis() - start);
		
		System.out.println("loaded " + total + " responses in " + time + " milliseconds.");
		System.out.println("free\t" + startingFreeMemory + "\ttotal\t" + startingTotalMemory + "\tmax\t" + startingMaxMemory + " at start");
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory() + " at finish");
		Runtime.getRuntime().gc();
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory() + " after GC");
	
	}
}
