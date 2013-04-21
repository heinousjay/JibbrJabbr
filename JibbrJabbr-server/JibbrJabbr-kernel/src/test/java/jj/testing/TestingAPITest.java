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

import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jj.JJ;

import org.junit.Rule;
import org.junit.Test;

/**
 * @author jason
 *
 */
public class TestingAPITest {
	
	static final String basePath;
	
	static {
		// well it's ugly, but it's portable
		basePath = Paths.get(JJ.uri(TestingAPITest.class)).getParent().getParent().getParent().toAbsolutePath().toString();
	}
	
	@Rule
	public JJAppTest app = new JJAppTest(basePath);
	
	@Test
	public void runBasicTest() throws Exception {
		
		TestHttpClient index = app.get("/index");
		index.dumpObjects();
		assertThat(index.status(), is(200));
		index.dumpObjects();
		assertThat(index.contentsString(), is(notNullValue()));
		
		assertThat(index.document().select("title").text(), is("JAYCHAT!"));
	}
	
	@Test
	public void runNotFoundTest() throws Exception {
		
		TestHttpClient client = app.get("/non-existent");
		assertThat(client.status(), is(404));
		
	}
	
	@Test
	public void getLotsOfClients() throws Exception {
		getLotsOfClients(3);
	}
	
	private boolean getLotsOfClients(final int number) throws Exception {
		
		TestHttpClient[] clients = new TestHttpClient[number * 2];
		int count = 0;
		for (int i = 0; i < number; ++i) {
			clients[count++] = app.get("/index");
			clients[count++] = app.get("/files");
		}
		
		count = 0;
		for (int i = 0; i < number; ++i) {
			assertThat(clients[count].status(), is(200));
			assertThat(clients[count].contentsString(), is(notNullValue()));
			assertThat(clients[count++].document().select("title").text(), is("JAYCHAT!"));
			
			assertThat(clients[count].status(), is(200));
			assertThat(clients[count].contentsString(), is(notNullValue()));
			assertThat(clients[count++].document().select("title").text(), is("files test"));
		}
		
		return true;
	}
	
	//@Test
	public void areYouKiddingMe() throws Throwable {
		final long startingTotalMemory = Runtime.getRuntime().totalMemory();
		final long startingMaxMemory = Runtime.getRuntime().maxMemory();
		final long startingFreeMemory = Runtime.getRuntime().freeMemory();
		final int totalClients = 4800;
		final int totalThreads = 12;
		final ExecutorService e = Executors.newFixedThreadPool(totalThreads);
		final long start = System.currentTimeMillis();
		
		final Callable<Boolean> c = new Callable<Boolean>() {
			
			public Boolean call() throws Exception {
				return getLotsOfClients(6);
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
		
		
		System.out.println("loaded " + (totalClients * 12) + " pages in " + (System.currentTimeMillis() - start) + " milliseconds.");
		System.out.println("free\t" + startingFreeMemory + "\ttotal\t" + startingTotalMemory + "\tmax\t" + startingMaxMemory + " at start");
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory());
		Runtime.getRuntime().gc();
		System.out.println("free\t" + Runtime.getRuntime().freeMemory() + "\ttotal\t" + Runtime.getRuntime().totalMemory() + "\tmax\t" + Runtime.getRuntime().maxMemory());
	}
	
	//@Test
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
