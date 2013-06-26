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
package jj.webbit;

import java.nio.ByteBuffer;
import java.util.Date;

import jj.resource.Resource;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.webbitserver.HttpResponse;
import org.webbitserver.wrapper.HttpResponseWrapper;

/**
 * @author jason
 *
 */
public class JJHttpResponse extends HttpResponseWrapper {

	private static final String MAX_AGE_ONE_YEAR = HttpHeaders.Values.MAX_AGE + "=" + String.valueOf(60 * 60 * 24 * 365);
	
	private final JJHttpRequest request;
	
	/**
	 * @param response
	 */
	public JJHttpResponse(final JJHttpRequest request, final HttpResponse response) {
		super(response);
		this.request = request;
	}
	
	/**
	 * sets the status of the outgoing response
	 * @param status
	 * @return
	 */
	public JJHttpResponse status(final HttpResponseStatus status) {
		status(status.getCode());
		return this;
	}
	
	public JJHttpResponse header(final String name, final String value) {
		super.header(name, value);
		return this;
	}
	
	public JJHttpResponse headerIfNotSet(final String name, final String value) {
		if (!containsHeader(name)) {
			super.header(name, value);
		}
		return this;
	}
	
	public JJHttpResponse header(final String name, final Date date) {
		super.header(name, date);
		return this;
	}
	
	public JJHttpResponse header(final String name, final long value) {
		super.header(name, value);
		return this;
	}
	
	public JJHttpResponse content(final byte[] bytes) {
		super.content(bytes);
		return this;
	}
	
	public JJHttpResponse content(final ByteBuffer bytes) {
		super.content(bytes);
		return this;
	}
	
	public JJHttpResponse end() {
		super.end();
		return this;
	}
	
	/**
	 * Sends a 304 Not Modified for the given resource and ends the response,
	 * allowing the result to be cached for one year
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendNotModified(final Resource resource) {
		return status(HttpResponseStatus.NOT_MODIFIED)
			.header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.LAST_MODIFIED, resource.lastModifiedDate())
			.end();
	}
	
	private String makeAbsoluteURL(final Resource resource) {
		return new StringBuilder("http")
			.append(request.secure() ? "s" : "")
			.append("://")
			.append(request.host())
			.append(resource.uri())
			.toString();
	}
	
	/**
	 * Sends a 307 Temporary Redirect to the given resource, using the fully qualified
	 * asset URL and not allowing the redirect to be cached
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendTemporaryRedirect(final Resource resource) {
		
		return status(HttpResponseStatus.TEMPORARY_REDIRECT)
			.header(HttpHeaders.Names.LOCATION, makeAbsoluteURL(resource))
			.header(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_STORE)
			.end();
	}
	
	/**
	 * Responds with the given resource metadata and bytes as a 200 OK, allowing the
	 * result to be cached for one year 
	 * @param resource
	 * @return
	 */
	public JJHttpResponse sendCachedResource(final Resource resource, ByteBuffer bytes) {
		return status(HttpResponseStatus.OK)
			.header(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_ONE_YEAR)
			.header(HttpHeaders.Names.ETAG, resource.sha1())
			.header(HttpHeaders.Names.LAST_MODIFIED, resource.lastModifiedDate())
			.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.limit())
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(bytes)
			.end();
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
	public JJHttpResponse sendUncachedResource(final Resource resource, byte[] bytes) {
		
		return status(HttpResponseStatus.OK)
			.headerIfNotSet(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE)
			.header(HttpHeaders.Names.CONTENT_LENGTH, bytes.length)
			.header(HttpHeaders.Names.CONTENT_TYPE, resource.mime())
			.content(bytes)
			.end();
	}
}
