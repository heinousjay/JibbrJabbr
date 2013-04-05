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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jj.Startup;
import jj.webbit.WebbitTestRunner;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.picocontainer.PicoContainer;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

/**
 * A test rule that supplies a context for performing
 * tests against a given HTML resource.
 * 
 * @author jason
 *
 */
public class JJTestRule implements TestRule {
	
	private final String basePath;
	
	private WebbitTestRunner testRunner;
	
	public JJTestRule(final String basePath) {
		this.basePath = basePath;
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				PicoContainer container = new Startup(new String[]{basePath}, true).container();
				testRunner = container.getComponent(WebbitTestRunner.class);
				try {
					base.evaluate();
				} finally {
					testRunner = null;
				}
			}
		};
	}
	
	public Document getAndWait(final String uri) throws Exception {
		return get(uri).get(10, TimeUnit.SECONDS);
	}
	
	public Document getAndWait(final String uri, final long time, final TimeUnit unit) throws Exception {
		return get(uri).get(time, unit);
	}
	
	public Future<Document> get(final String uri) throws Exception {
		
		if (testRunner == null) throw new AssertionError("somehow this happened?");
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		final StubHttpRequest stubHttpRequest = 
			new StubHttpRequest(uri)
			.timestamp(System.nanoTime())
			.header(HttpHeaders.Names.HOST, "localhost");
		
		final StubHttpResponse stubHttpResponse = new StubHttpResponse() {
			
			@Override
			public StubHttpResponse end() {
				latch.countDown();
				return super.end();
			}
		};
		
		testRunner.executeRequest(stubHttpRequest, stubHttpResponse);
		
		return new Future<Document>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return latch.getCount() < 1;
			}

			@Override
			public Document get() throws InterruptedException, ExecutionException {
				latch.await();
				return Jsoup.parse(stubHttpResponse.contentsString());
			}

			@Override
			public Document get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				if (latch.await(timeout, unit)) {
					return Jsoup.parse(stubHttpResponse.contentsString());
				}
				throw new TimeoutException("timed out in " + timeout + " " + unit.toString().toLowerCase());
			}
			
		};
	}
}
