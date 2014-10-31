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

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jj.http.server.EmbeddedHttpServer.ResponseReady;

/**
 * @author jason
 *
 */
public class EmbeddedHttpResponse {
	
	private final ResponseReady responseReady;
	
	private final CountDownLatch responded = new CountDownLatch(1);
	
	volatile Throwable error;
	
	volatile HttpResponse response;
	
	final CompositeByteBuf buffer = ByteBufAllocator.DEFAULT.compositeBuffer();

	EmbeddedHttpResponse() {
		this(null);
	}
	
	EmbeddedHttpResponse(final ResponseReady responseReady) {
		this.responseReady = responseReady;
	}
	
	private void checkError() throws Throwable {
		if (error != null) {
			throw error;
		}
	}
	
	public EmbeddedHttpResponse await(long time, TimeUnit unit) throws Throwable {
		if (!responded.await(time, unit)) {
			throw new AssertionError("timed out");
		}
		checkError();
		
		return this;
	}
	
	void responseReady() throws Exception {
		responded.countDown();
		if (responseReady != null) {
			responseReady.ready(this);
		}
	}
	
	public HttpHeaders headers() throws Throwable {
		checkError();
		return (responded.getCount() == 0) ? response.headers() : null;
	}
	
	public HttpResponseStatus status() throws Throwable {
		checkError();
		return (responded.getCount() == 0) ? response.status() : null;
	}
	
	public String bodyContentAsString() throws Throwable {
		checkError();
		if (responded.getCount() == 0) {
			
			ContentTypeHeaderReader cth = new ContentTypeHeaderReader(response.headers());
			assert !cth.isBadRequest() : "response was not understood";
			return cth.isText() ? buffer.readerIndex(0).toString(cth.charset()) : null;
		}
		
		return null;
	}
	
	public byte[] bodyContentAsBytes() throws Throwable {
		checkError();
		if (responded.getCount() == 0) {
			buffer.readerIndex(0);
			byte[] result = new byte[buffer.readableBytes()];
			buffer.readBytes(result);
			
			return result;
		}
		
		return null;
	}
}
