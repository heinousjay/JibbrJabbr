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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jj.JJ;
import jj.resource.AssetResource;

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
	
	/**
	 * 
	 */
	private static final String INDEX_HTML_RENDERED = "index.html.rendered";
	private static final String ANIMAL_HTML_RENDERED = "animal.html.rendered";
	/**
	 * 
	 */
	private static final String TITLE = "title";
	/**
	 * 
	 */
	private static final String INDEX = "/index";
	/**
	 * 
	 */
	private static final String ANIMAL = "/animal";
	static final String INDEX_TITLE = "API TEST SUCCESS";
	static final String ANIMAL_TITLE = "ANIMAL!";
	
	static final String basePath;
	
	static {
		// well it's ugly, but it's portable
		basePath = Paths.get(JJ.uri(TestingAPITest.class)).getParent().getParent().getParent().toAbsolutePath().toString();
	}
	
	@Rule
	public JJAppTest app = new JJAppTest(basePath);
	
	static interface Namer {
		String name(int i);
		Path path(int i);
	}
	
	private final Path indexHtmlRenderedPath = Paths.get(basePath, INDEX_HTML_RENDERED);
	private final Path animalHtmlRenderedPath = Paths.get(basePath, ANIMAL_HTML_RENDERED);
	
	private void runStressTestPattern(int totalClients, Namer namer) throws Exception {
		TestHttpClient[] clients = new TestHttpClient[totalClients];
		for (int i = 0; i < clients.length; ++i) {
			clients[i] = app.get(namer.name(i));
		}
		
		for (int i = 0; i < clients.length; ++i) {
			try {
				assertThat(clients[i].status(), is(HttpResponseStatus.OK));
				assertThat(namer.name(i), clients[i].contentBytes(), is(Files.readAllBytes(namer.path(i))));
			} catch (AssertionError e) {
				System.out.print(new StringBuilder()
					.append(i).append(System.lineSeparator())
					.append(namer.name(i)).append(System.lineSeparator())
					.append(clients[i].contentsString()).append(System.lineSeparator())
					.append(namer.path(i)).append(System.lineSeparator())
				);
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
	
	@Test
	public void runDocumentTest() throws Exception {
		runStressTestPattern(300, new Namer() {

			@Override
			public String name(int i) {
				return INDEX;
			}

			@Override
			public Path path(int i) {
				return indexHtmlRenderedPath;
			}
			
		});
	}
	
	@Test
	public void runStaticTest() throws Exception {
		runStressTestPattern(300, new Namer() {

			@Override
			public String name(int i) {
				return "/" + (i % 10) + ".txt";
			}
			
			@Override
			public Path path(int i) {
				return Paths.get(basePath, name(i));
			}
			
		});
	}
	
	@Test
	public void runCssTest() throws Exception {
		runStressTestPattern(300, new Namer() {

			@Override
			public String name(int i) {
				return "/" + (i % 2 == 0 ? "style.css" : "test.css");
			}
			
			@Override
			public Path path(int i) {
				return Paths.get(basePath, name(i));
			}
			
		});
	}
	
	@Test
	public void runAssetTest() throws Exception {
		runStressTestPattern(300, new Namer() {

			private final String[] names = {
				JQUERY_JS,
				JJ_JS,
				FAVICON_ICO
			};
			
			@Override
			public String name(int i) {
				return "/" + names[i % names.length];
			}
			
			@Override
			public Path path(int i) {
				return Paths.get(JJ.uri(AssetResource.class)).getParent().getParent().resolve("assets").resolve(names[i % names.length]);
			}
			
		});
	}
	
	@Test
	public void runMixedTest() throws Exception {
		runStressTestPattern(350, new Namer() {

			private final String[] names = {
				JQUERY_JS,
				JJ_JS,
				FAVICON_ICO,
				"test.css",
				"style.css",
				"2.txt",
				INDEX.substring(1)
			};

			@Override
			public String name(int i) {
				return "/" + names[i % names.length];
			}

			@Override
			public Path path(int i) {
				switch(i % names.length) {
				case 0: case 1: case 2:
					return Paths.get(JJ.uri(AssetResource.class)).getParent().getParent().resolve("assets").resolve(names[i % names.length]);
				case 3: case 4: case 5:
					return Paths.get(basePath, names[i % names.length]);
				case 6:
					return indexHtmlRenderedPath;
				default:
					throw new AssertionError("something went really wrong");
				}
			}
			
		});
	}
	
	@Test
	public void runBasicTest() throws Exception {
		
		TestHttpClient index = app.get(INDEX);
		TestHttpClient animal = app.get(ANIMAL);
		
		// just fire off a bunch of requests we ignore
		// in the meantime
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		app.get(INDEX);
		app.get(ANIMAL);
		
		
		// TODO - add more interesting verifications
		// TODO - use the http client to do this instead?
		assertThat(index.status(), is(HttpResponseStatus.OK));
		assertThat(index.contentsString(), is(notNullValue()));
		assertThat(index.document().select(TITLE).text(), is(INDEX_TITLE));
		
		assertThat(animal.status(), is(HttpResponseStatus.OK));
		assertThat(animal.contentsString(), is(notNullValue()));
		assertThat(animal.document().select(TITLE).text(), is(ANIMAL_TITLE));
		
		index = app.get(INDEX);
		animal = app.get(ANIMAL);
		
		assertThat(index.status(), is(HttpResponseStatus.OK));
		assertThat(index.contentsString(), is(notNullValue()));
		assertThat(index.document().select(TITLE).text(), is(INDEX_TITLE));
		
		assertThat(animal.status(), is(HttpResponseStatus.OK));
		assertThat(animal.contentsString(), is(notNullValue()));
		assertThat(animal.document().select(TITLE).text(), is(ANIMAL_TITLE));
	}
	
	@Test
	public void getLotsOfDocuments() throws Exception {
		getLotsOfDocuments(200);
	}
	
	private boolean getLotsOfDocuments(final int number) throws Exception {
		
		TestHttpClient[] clients = new TestHttpClient[number * 2];
		int count = 0;
		for (int i = 0; i < number; ++i) {
			clients[count++] = app.get(INDEX);
			clients[count++] = app.get(ANIMAL);
		}
		
		count = 0;
		for (int i = 0; i < number; ++i) {
			String display = String.valueOf(count) + ": " + clients[count].uri();
			try {
				assertThat(display, clients[count].status(), is(HttpResponseStatus.OK));
				assertThat(display, clients[count].contentsString(), is(notNullValue()));
				assertThat(display, clients[count++].document().select(TITLE).text(), is(INDEX_TITLE));
			} catch (Exception e) {
				System.err.println(display);
				throw e;
			}
			
			display = String.valueOf(count) + ": " + clients[count].uri();
			try {
				assertThat(display, clients[count].status(), is(HttpResponseStatus.OK));
				assertThat(display, clients[count].contentsString(), is(notNullValue()));
				assertThat(display, clients[count++].document().select(TITLE).text(), is(ANIMAL_TITLE));
			} catch (Exception e) {
				System.err.println(display);
				throw e;
			}
		}
		
		return true;
	}
	
	@Test
	public void poundIt() throws Exception {
		poundIt(12, 4000);
	}
	
	public void poundIt(final int threadCount, final int perClientRequestCount) throws Exception {
		final CountDownLatch latch = new CountDownLatch(threadCount);
		ExecutorService service = Executors.newFixedThreadPool(threadCount);
		final Throwable[] throwables = new Throwable[threadCount];
		try {
			for (int i = 0; i < threadCount; ++i) {
				final int index = i;
				service.submit(new Runnable() {
					
					@Override
					public void run() {
						try {
							runStressTestPattern(perClientRequestCount, new Namer() {

								private final String[] names = {
									JQUERY_JS,
									JJ_JS,
									FAVICON_ICO,
									"test.css",
									"style.css",
									"2.txt",
									INDEX.substring(1),
									ANIMAL.substring(1)
								};

								@Override
								public String name(int i) {
									return "/" + names[i % names.length];
								}

								@Override
								public Path path(int i) {
									switch(i % names.length) {
									case 0: case 1: case 2:
										return Paths.get(JJ.uri(AssetResource.class)).getParent().getParent().resolve("assets").resolve(names[i % names.length]);
									case 3: case 4: case 5:
										return Paths.get(basePath, names[i % names.length]);
									case 6:
										return indexHtmlRenderedPath;
									case 7:
										return animalHtmlRenderedPath;
									default:
										throw new AssertionError("something went really wrong");
									}
								}
								
							});
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
