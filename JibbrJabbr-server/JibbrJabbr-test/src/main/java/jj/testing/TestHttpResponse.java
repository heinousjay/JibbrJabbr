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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.mock;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import jj.DateFormatHelper;
import jj.ExecutionTrace;
import jj.logging.AccessLogger;
import jj.logging.TestRunnerLogger;
import jj.resource.MimeTypes;
import jj.http.JJHttpResponse;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

/**
 * @author jason
 *
 */
class TestHttpResponse extends JJHttpResponse {
	
	private final CountDownLatch latch = new CountDownLatch(1);
	
	private int id = 0;
	
	private TestHttpRequest request;
	
	private final ExecutionTrace trace;
	
	private final Logger testRunnerLogger;
	
	private final AtomicBoolean gotOnce = new AtomicBoolean(true);
	
	@Inject
	TestHttpResponse(
		final TestHttpRequest request,
		final ExecutionTrace trace,
		final @TestRunnerLogger Logger testRunnerLogger,
		final @AccessLogger Logger accessLogger
	) {
		super(request, mock(Channel.class), accessLogger);
		this.trace = trace;
		this.testRunnerLogger = testRunnerLogger;
	}
	
	void id(int id) {
		this.id = id;
	}
	
	int id() {
		return id;
	}
	
	public TestHttpResponse end() {
		testRunnerLogger.info("end called on {}", this);
		processResponse();
		return this;
	}

	public TestHttpResponse error(Throwable t) {
		testRunnerLogger.info("error called on {}", this);
		processResponse();
		return this;
	}
	
	public boolean isDone() {
		return latch.getCount() < 1;
	}
	
	private final AtomicReference<Document> document = new AtomicReference<>();
	
	public Document document() {
		return document.get();
	}
	
	private void processResponse() {

		if (MimeTypes.get("html").equalsIgnoreCase(header(HttpHeaders.Names.CONTENT_TYPE))) {
			if (!document.compareAndSet(null, Jsoup.parse(contentsString()))) {
				new AssertionError("document was not null").printStackTrace();
			}
		}
		
		trace.end(request);
		latch.countDown();
	}

	/**
	 * waits until the server has responded, returning true to the first call to this
	 * method or the timeout version and false to every subsequent call
	 * @return
	 * @throws Exception
	 */
	public boolean get() throws Exception {

		return get(2, SECONDS);
	}

	public boolean get(long timeout, TimeUnit unit) throws Exception {
		if (!isDone()) {
			if (!latch.await(timeout, unit)) {
				throw new TimeoutException("timed out in " + timeout + " " + unit.toString().toLowerCase());
			}
		}
		return gotOnce.getAndSet(false);
	}
	
	@Override
	public String toString() {
		return new StringBuilder(TestHttpResponse.class.getSimpleName())
			.append("[").append(id()).append("] {")
			.append("charset=").append(charset())
			.append(", status=").append(status())
			.append(", headers=").append(response.headers())
			.append(", error=").append(error())
			.append(", ended=").append(ended())
			.append(", contents size=").append(response.content().readableBytes())
			.append("}")
			.toString();
	}

	/**
	 * @return
	 */
	public boolean ended() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public Throwable error() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 */
	public Charset charset() {
		// TODO Auto-generated method stub
		return null;
	}
}