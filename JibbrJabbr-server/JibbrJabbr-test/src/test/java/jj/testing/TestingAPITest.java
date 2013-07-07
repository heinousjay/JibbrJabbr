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

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jj.JJ;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class TestingAPITest {
	
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
	static final String INDEX_TITLE = "TITLE GOES HERE";
	static final String ANIMAL_TITLE = "ANIMAL!";
	
	static final String basePath;
	
	static {
		// well it's ugly, but it's portable
		basePath = Paths.get(JJ.uri(TestingAPITest.class)).getParent().getParent().getParent().toAbsolutePath().toString();
	}
	
	@Rule
	public JJAppTest app = new JJAppTest(basePath);
	
	private String loadText(int number) throws Exception {
		return new String(Files.readAllBytes(Paths.get(basePath, (number % 10) + ".txt")), StandardCharsets.UTF_8);
	}
	
	@Ignore @Test
	public void runStaticTest() throws Exception {
		TestHttpClient[] clients = new TestHttpClient[100];
		HttpHeaders headers = new DefaultHttpHeaders();
		for (int i = 0; i < clients.length; ++i) {
			clients[i] = app.get("/" + (i % 10) + ".txt");
		}
		
		for (int i = 0; i < clients.length; ++i) {
			assertThat(clients[i].contentsString(), is(loadText(i)));
			clients[i].matchesHeaders(headers);
		}
	}
	
	@Test
	public void runBasicTest() throws Exception {
		
		TestHttpClient index = app.get(INDEX);
		TestHttpClient animal = app.get(ANIMAL);
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
		app.get(ANIMAL).contentsString();
		
		//index.dumpObjects();
		assertThat(index.status(), is(HttpResponseStatus.OK));
		//index.dumpObjects();
		assertThat(index.contentsString(), is(notNullValue()));
		
		assertThat(index.document().select(TITLE).text(), is(INDEX_TITLE));
		//animal.dumpObjects();
		assertThat(animal.status(), is(HttpResponseStatus.OK));
		//animal.dumpObjects();
		assertThat(animal.contentsString(), is(notNullValue()));
		
		assertThat(animal.document().select(TITLE).text(), is(ANIMAL_TITLE));
		
		index = app.get(INDEX);
		animal = app.get(ANIMAL);
		//index.dumpObjects();
		assertThat(index.status(), is(HttpResponseStatus.OK));
		//index.dumpObjects();
		assertThat(index.contentsString(), is(notNullValue()));
		
		assertThat(index.document().select(TITLE).text(), is(INDEX_TITLE));
		//animal.dumpObjects();
		assertThat(animal.status(), is(HttpResponseStatus.OK));
		//animal.dumpObjects();
		assertThat(animal.contentsString(), is(notNullValue()));
		
		assertThat(animal.document().select(TITLE).text(), is(ANIMAL_TITLE));
	}
	
	@Test
	public void getLotsOfClients() throws Exception {
		getLotsOfClients(3);
	}
	
	private boolean getLotsOfClients(final int number) throws Exception {
		
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
	
	@Ignore @Test
	public void areYouKiddingMe() throws Throwable {
		final long startingTotalMemory = Runtime.getRuntime().totalMemory();
		final long startingMaxMemory = Runtime.getRuntime().maxMemory();
		final long startingFreeMemory = Runtime.getRuntime().freeMemory();
		final int totalClients = 4800; // total number of clients made.  this * totalThreads is how many
		// requests total will be made
		final int totalThreads = 12; // must always be even, represents the total number of in-flight requests
		final ExecutorService e = Executors.newFixedThreadPool(totalThreads);
		final long start = System.currentTimeMillis();
		
		final Callable<Boolean> c = new Callable<Boolean>() {
			
			public Boolean call() throws Exception {
				return getLotsOfClients(totalThreads / 2);
			};
		};
		
		@SuppressWarnings("unchecked")
		Future<Boolean>[] f = (Future<Boolean>[]) new Future<?>[totalClients];
		for (int i = 0; i < totalClients; ++i) {
			f[i] = e.submit(c);
		}
		
		boolean success = true;
		try {
			for (int i = 0; i < totalClients; ++i) {
				success = f[i].get() && success;
			}
		} catch (ExecutionException er) {
			throw er.getCause();
		} finally {
			e.shutdownNow();
		}
		
		if (!success) throw new AssertionError("NOT SUCCESS, somehow");
		
		
		System.out.println("loaded " + (totalClients * totalThreads) + " pages in " + (System.currentTimeMillis() - start) + " milliseconds.");
		System.out.println("free\t" + startingFreeMemory + "\ttotal\t" + startingTotalMemory + "\tmax\t" + startingMaxMemory + " at start");
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory() + " at finish");
		Runtime.getRuntime().gc();
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory() + " after GC");
	}
	
	@Ignore @Test
	public void areYouKiddingMePart2() throws Throwable {
		areYouKiddingMe();
		System.out.println();
		System.out.println("the numbers mean something starting here");
		System.out.println();
		areYouKiddingMe();
		areYouKiddingMe();
		areYouKiddingMe();
	}
}
