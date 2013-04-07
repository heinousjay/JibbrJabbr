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
		
		TestClient index = app.get("/index");
		index.dumpObjects();
		assertThat(index.status(), is(200));
		index.dumpObjects();
		assertThat(index.contentsString(), is(notNullValue()));
		
		assertThat(index.document().select("title").text(), is("JAYCHAT!"));
	}
	
	@Test
	public void runNotFoundTest() throws Exception {
		
		TestClient client = app.get("/non-existent");
		assertThat(client.status(), is(404));
		
	}
	
	@Test
	public void getLotsOfClients() throws Exception {
		TestClient index1 = app.get("/index");
		TestClient index2 = app.get("/files");
		TestClient index3 = app.get("/index");
		TestClient index4 = app.get("/files");
		TestClient index5 = app.get("/index");
		TestClient index6 = app.get("/files");
		TestClient index7 = app.get("/index");
		TestClient index8 = app.get("/files");
		TestClient index9 = app.get("/index");
		TestClient index10 = app.get("/files");
		TestClient index11 = app.get("/index");
		TestClient index12 = app.get("/files");
		
		assertThat(index1.status(), is(200));
		assertThat(index1.contentsString(), is(notNullValue()));
		assertThat(index1.document().select("title").text(), is("JAYCHAT!"));
		
		assertThat(index2.status(), is(200));
		assertThat(index2.contentsString(), is(notNullValue()));
		assertThat(index2.document().select("title").text(), is("files test"));
		
		assertThat(index3.status(), is(200));
		assertThat(index3.contentsString(), is(notNullValue()));
		assertThat(index3.document().select("title").text(), is("JAYCHAT!"));
		
		assertThat(index4.status(), is(200));
		assertThat(index4.contentsString(), is(notNullValue()));
		assertThat(index4.document().select("title").text(), is("files test"));
		
		assertThat(index5.status(), is(200));
		assertThat(index5.contentsString(), is(notNullValue()));
		assertThat(index5.document().select("title").text(), is("JAYCHAT!"));
		
		assertThat(index6.status(), is(200));
		assertThat(index6.contentsString(), is(notNullValue()));
		assertThat(index6.document().select("title").text(), is("files test"));
		
		assertThat(index7.status(), is(200));
		assertThat(index7.contentsString(), is(notNullValue()));
		assertThat(index7.document().select("title").text(), is("JAYCHAT!"));
		
		assertThat(index8.status(), is(200));
		assertThat(index8.contentsString(), is(notNullValue()));
		assertThat(index8.document().select("title").text(), is("files test"));
		
		assertThat(index9.status(), is(200));
		assertThat(index9.contentsString(), is(notNullValue()));
		assertThat(index9.document().select("title").text(), is("JAYCHAT!"));
		
		assertThat(index10.status(), is(200));
		assertThat(index10.contentsString(), is(notNullValue()));
		assertThat(index10.document().select("title").text(), is("files test"));
		
		assertThat(index11.status(), is(200));
		assertThat(index11.contentsString(), is(notNullValue()));
		assertThat(index11.document().select("title").text(), is("JAYCHAT!"));
		
		assertThat(index12.status(), is(200));
		assertThat(index12.contentsString(), is(notNullValue()));
		assertThat(index12.document().select("title").text(), is("files test"));
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
		
		final Callable<Void> c = new Callable<Void>() {
			
			public Void call() throws Exception {
				getLotsOfClients();
				return null;
			};
		};
		
		@SuppressWarnings("unchecked")
		Future<Void>[] f = (Future<Void>[]) new Future<?>[totalClients];
		for (int i = 0; i < totalClients; ++i) {
			f[i] = e.submit(c);
		}
		
		try {
			for (int i = 0; i < totalClients; ++i) {
				f[i].get();
			}
		} catch (ExecutionException er) {
			throw er.getCause();
		} finally {
			e.shutdownNow();
		}
		
		
		System.out.println("loaded " + (totalClients * 8) + " pages in " + (System.currentTimeMillis() - start) + " milliseconds.");
		System.out.println("starting free " + startingFreeMemory + " total " + startingTotalMemory + " max " + startingMaxMemory);
		System.out.println("now free " + Runtime.getRuntime().freeMemory() + " total " + Runtime.getRuntime().totalMemory() + " max " + Runtime.getRuntime().maxMemory());
		Runtime.getRuntime().gc();
	}
	
	//@Test
	public void areYouKiddingMePart2() throws Throwable {
		areYouKiddingMe();
	}
}
