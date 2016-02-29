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

import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map.Entry;

/**
 * @author jason
 *
 */
public interface HttpServerResponse {
	
	public static final String MAX_AGE_ONE_YEAR = HttpHeaderValues.MAX_AGE + "=" + String.valueOf(60 * 60 * 24 * 365);

	/**
	 * Retrieve the status of the outgoing response.  Defaults to
	 * {@code 200 OK}.
	 * @return
	 */
	HttpResponseStatus status();

	/**
	 * sets the status of the outgoing response.
	 * @param status
	 * @return
	 */
	HttpServerResponse status(HttpResponseStatus status);

	/**
	 * Set the header by name and value. This method will
	 * add a header if one already exists.
	 * @param name
	 * @param value
	 * @return
	 */
	HttpServerResponse header(AsciiString name, CharSequence value);
	
	/**
	 * Set the header by name and value, if no header by this
	 * name exists
	 * @param name
	 * @param value
	 * @return
	 */
	HttpServerResponse headerIfNotSet(AsciiString name, CharSequence value);

	/**
	 * Set the header by name and value, if no header by this
	 * name exists
	 * @param name
	 * @param value
	 * @return
	 */
	HttpServerResponse headerIfNotSet(AsciiString name, long value);

	/**
	 * @param name
	 * @return
	 */
	boolean containsHeader(AsciiString name);

	HttpServerResponse header(AsciiString name, Date date);

	HttpServerResponse header(AsciiString name, long value);

	CharSequence header(AsciiString name);

	Iterable<Entry<String, String>> allHeaders();

	HttpVersion version();

	HttpServerResponse content(byte[] bytes);

	HttpServerResponse content(ByteBuf buffer);

	HttpServerResponse end();

	void sendNotFound();

	void sendError(HttpResponseStatus status);

	/**
	 * Sends a 304 Not Modified for the given resource and ends the response
	 * @param resource
	 * @return
	 */
	HttpServerResponse sendNotModified(ServableResource resource);

	HttpServerResponse sendNotModified(ServableResource resource, boolean cache);

	/**
	 * Sends a 307 Temporary Redirect to the given resource, using the fully qualified
	 * asset URL and disallowing the redirect to be cached
	 * @param resource
	 * @return
	 */
	HttpServerResponse sendTemporaryRedirect(ServableResource resource);

	/**
	 * @param e
	 */
	HttpServerResponse error(Throwable e);

	/**
	 * @return
	 */
	Charset charset();

	/**
	 * @return
	 */
	String contentsString();

	HttpServerResponse sendUncachableResource(ServableResource resource) throws IOException;

	HttpServerResponse sendCachableResource(ServableResource resource) throws IOException;

}