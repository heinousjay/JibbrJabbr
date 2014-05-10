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
package jj.http.server;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import jj.resource.MimeTypes;
import jj.resource.Resource;
import jj.resource.TransferableResource;
import jj.testing.TestLog;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author jason
 *
 */
public class TestHttpResponse extends AbstractHttpResponse {
	
	private final CountDownLatch latch = new CountDownLatch(1);
	
	private int id = 0;
	
	private final TestLog testLog;
	
	private final AtomicBoolean gotOnce = new AtomicBoolean(true);
	
	@Inject
	TestHttpResponse(
		final TestLog testLog
	) {
		this.testLog = testLog;
	}
	
	void id(int id) {
		this.id = id;
	}
	
	public int id() {
		return id;
	}
	
	@Override
	protected String makeAbsoluteURL(Resource resource) {
		return "http://localhost/" + resource.uri();
	}
	
	private volatile boolean ended = false;
	
	public TestHttpResponse end() {
		markCommitted();
		testLog.trace("request ended");
		processResponse();
		ended = true;
		return this;
	}

	/**
	 * @return
	 */
	public boolean ended() {
		return ended;
	}
	
	private volatile Throwable error = null;

	public TestHttpResponse error(Throwable t) {
		testLog.info("request errored", t);
		sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		error = t;
		return this;
	}

	/**
	 * @return
	 */
	public Throwable error() {
		return error;
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
			.append(", contents size=").append(content().readableBytes())
			.append("}")
			.toString();
	}

	@Override
	protected HttpResponse doSendTransferableResource(TransferableResource resource) throws IOException {
		try (FileChannel channel = resource.fileChannel()) {
			
			assert channel.size() < 1_000_000L;
			
			ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
			while (buffer.position() < buffer.capacity()) {
				channel.read(buffer);
			}
			content(buffer.array());
		}
		return end();
	}
	
	public HttpHeaders headers() {
		return response.headers();
	}
	
	public byte[] contentBytes() {
		byte[] contentBytes = new byte[content().readableBytes()];
		content().slice().readBytes(contentBytes);
		return contentBytes;
	}
}
