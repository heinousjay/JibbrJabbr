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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.webbitserver.stub.StubHttpResponse;

/**
 * @author jason
 *
 */
class TestHttpResponse extends StubHttpResponse implements Future<Document> {
	
	private final CountDownLatch latch = new CountDownLatch(1);
	
	@Override
	public StubHttpResponse end() {
		latch.countDown();
		return super.end();
	}
	@Override
	public StubHttpResponse error(Throwable t) {
		latch.countDown();
		return super.error(t);
	}
	
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
		return Jsoup.parse(contentsString());
	}

	@Override
	public Document get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (latch.await(timeout, unit)) {
			return Jsoup.parse(contentsString());
		}
		throw new TimeoutException("timed out in " + timeout + " " + unit.toString().toLowerCase());
	}
}
