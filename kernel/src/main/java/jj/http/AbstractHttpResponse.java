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
package jj.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import jj.resource.LoadedResource;
import jj.resource.Resource;
import jj.resource.TransferableResource;

/**
 * Used to generate an HTTP response.  
 * @author jason
 *
 */
public abstract class AbstractHttpResponse implements HttpResponse {

	public static final String MAX_AGE_ONE_YEAR = HttpHeaders.Values.MAX_AGE + "=" + String.valueOf(60 * 60 * 24 * 365);
	protected final DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	protected AtomicReference<ByteBuf> content = new AtomicReference<>();
	private volatile boolean isCommitted = false;
	protected final Charset charset = StandardCharsets.UTF_8;

	/**
	 * 
	 */
	protected AbstractHttpResponse() {
		super();
	}

	protected void assertNotCommitted() {
		assert !isCommitted : "response has already been committed.  modification is not permitted";
	}
	
	protected void markCommitted() {
		this.isCommitted = true;
	}

	@Override
	public HttpResponseStatus status() {
		return response.getStatus();
	}

	/**
	 * sets the status of the outgoing response
	 * @param status
	 * @return
	 */
	@Override
	public HttpResponse status(final HttpResponseStatus status) {
		assertNotCommitted();
		response.setStatus(status);
		return this;
	}

	@Override
	public HttpResponse header(final String name, final String value) {
		assertNotCommitted();
		response.headers().add(name, value);
		return this;
	}

	@Override
	public HttpResponse headerIfNotSet(final String name, final String value) {
		assertNotCommitted();
		if (!containsHeader(name)) {
			header(name, value);
		}
		return this;
	}

	@Override
	public HttpResponse headerIfNotSet(final String name, final long value) {
		assertNotCommitted();
		if (!containsHeader(name)) {
			header(name, value);
		}
		return this;
	}

	/**
	 * @param name
	 * @return
	 */
	@Override
	public boolean containsHeader(String name) {
		return response.headers().contains(name);
	}

	@Override
	public HttpResponse header(final String name, final Date date) {
		assertNotCommitted();
		response.headers().add(name, date);
		return this;
	}

	@Override
	public HttpResponse header(final String name, final long value) {
		assertNotCommitted();
		response.headers().add(name, value);
		return this;
	}

	/**
	 * @return
	 */
	@Override
	public List<Entry<String, String>> allHeaders() {
		// TODO make unmodifiable if committed
		return response.headers().entries();
	}

	@Override
	public HttpVersion version() {
		return response.getProtocolVersion();
	}
	
	protected ByteBuf content() {
		if (content.get() == null) {
			content.compareAndSet(null, Unpooled.buffer(0));
		}
		return content.get();
	}

	@Override
	public HttpResponse content(final byte[] bytes) {
		assertNotCommitted();
		content().writeBytes(bytes);
		return this;
	}

	@Override
	public HttpResponse content(final ByteBuf buffer) {
		assertNotCommitted();
		content().writeBytes(buffer);
		return this;
	}

	@Override
	public String header(String name) {
		return response.headers().get(name);
	}

	/**
	 * @return
	 */
	@Override
	public Charset charset() {
		return charset;
	}

	/**
	 * @return
	 */
	@Override
	public String contentsString() {
		return content().toString(charset);
	}

	@Override
	public void sendError(final HttpResponseStatus status) {
		assertNotCommitted();
		byte[] body = status.reasonPhrase().getBytes(StandardCharsets.UTF_8);
		status(status)
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, body.length)
			.header(HttpHeaders.Names.CONTENT_TYPE, "text/plain; UTF-8")
			.content(body)
			.end();
	}

	@Override
	public void sendNotFound() {
		assertNotCommitted();
		sendError(HttpResponseStatus.NOT_FOUND);
	}

	/**
	 * Sends a 304 Not Modified for the given resource with no cache headers. Ends
	 * the response.
	 * @param resource
	 * @return
	 */
	@Override
	public HttpResponse sendNotModified(final Resource resource) {
		return sendNotModified(resource, false);
	}

	/**
	 * Sends a 304 Not Modified for the given resource, caching the response for
	 * one year if {@code cache} is true.  Ends the response.
	 * @param resource
	 * @param cache
	 * @return
	 */
	@Override
	public HttpResponse sendNotModified(final Resource resource, boolean cache) {
		assertNotCommitted();
		
		if (cache) {
			header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR);
		}
		
		return status(HttpResponseStatus.NOT_MODIFIED)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.end();
	}
	
	/**
	 * used to generate redirects
	 * @param resource
	 * @return
	 */
	protected abstract String makeAbsoluteURL(final Resource resource);

	/**
	 * Sends a 307 Temporary Redirect to the given resource, using the fully qualified
	 * asset URL and disallowing the redirect to be cached.  Ends the response.
	 * @param resource
	 * @return
	 */
	@Override
	public HttpResponse sendTemporaryRedirect(final Resource resource) {
		assertNotCommitted();
		return status(HttpResponseStatus.TEMPORARY_REDIRECT)
			.header(HttpHeaders.Names.LOCATION, makeAbsoluteURL(resource))
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.end();
	}
	
	protected abstract HttpResponse doSendTransferableResource(TransferableResource resource) throws IOException;

	@Override
	public HttpResponse sendUncachedResource(Resource resource) throws IOException {
		assertNotCommitted();
		header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE);
		if (resource instanceof TransferableResource) {
			return sendResource((TransferableResource)resource);
		} else if (resource instanceof LoadedResource) {
			return sendResource((LoadedResource)resource);
		}
		
		throw new AssertionError("trying to send a resource I don't understand");
	}

	@Override
	public HttpResponse sendCachedResource(Resource resource) throws IOException {
		assertNotCommitted();
		header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR);
		if (resource instanceof TransferableResource) {
			return sendResource((TransferableResource)resource);
		} else if (resource instanceof LoadedResource) {
			return sendResource((LoadedResource)resource);
		}
		
		throw new AssertionError("trying to send a resource I don't understand");
	}

	/**
	 * Responds with the given resource and bytes as a 200 OK, not setting any
	 * validation headers and turning caching off if no cache control headers have
	 * previously been set on the response.  this is the appropriate responding
	 * method for dynamically generated responses (not including simple statically
	 * compiled dynamic resources, like less->css)
	 * 
	 * @param resource
	 * @param bytes
	 * @return
	 */
	protected HttpResponse sendResource(final LoadedResource resource) {
		return header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.bytes().readableBytes())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(resource.bytes())
			.end();
	}

	/**
	 * Transfers a resource to the connected client using the operating system
	 * zero-copy facilities.
	 * 
	 * @param resource
	 * @return
	 */
	protected HttpResponse sendResource(TransferableResource resource) throws IOException {
		header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.header(HttpHeaders.Names.CONTENT_LENGTH, resource.size())
			.header(HttpHeaders.Names.DATE, new Date());
		
		if (resource.sha1() != null) {
			header(HttpHeaders.Names.ETAG, resource.sha1());
		}
		
		return doSendTransferableResource(resource);
	}

}