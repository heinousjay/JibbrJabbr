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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import jj.resource.Resource;

/**
 * @author jason
 *
 */
public interface HttpResponse {

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
	HttpResponse status(HttpResponseStatus status);

	/**
	 * Set the header by name and value. This method will
	 * add a header if one already exists.
	 * @param name
	 * @param value
	 * @return
	 */
	HttpResponse header(String name, String value);
	
	/**
	 * Set the header by name and value, if no header by this
	 * name exists
	 * @param name
	 * @param value
	 * @return
	 */
	HttpResponse headerIfNotSet(String name, String value);

	/**
	 * Set the header by name and value, if no header by this
	 * name exists
	 * @param name
	 * @param value
	 * @return
	 */
	HttpResponse headerIfNotSet(String name, long value);

	/**
	 * @param name
	 * @return
	 */
	boolean containsHeader(String name);

	HttpResponse header(String name, Date date);

	HttpResponse header(String name, long value);

	/**
	 * @return
	 */
	List<Entry<String, String>> allHeaders();

	HttpVersion version();

	HttpResponse content(byte[] bytes);

	HttpResponse content(ByteBuf buffer);

	HttpResponse end();

	void sendNotFound();

	void sendError(HttpResponseStatus status);

	/**
	 * Sends a 304 Not Modified for the given resource and ends the response
	 * @param resource
	 * @return
	 */
	HttpResponse sendNotModified(Resource resource);

	HttpResponse sendNotModified(Resource resource, boolean cache);

	/**
	 * Sends a 307 Temporary Redirect to the given resource, using the fully qualified
	 * asset URL and disallowing the redirect to be cached
	 * @param resource
	 * @return
	 */
	HttpResponse sendTemporaryRedirect(Resource resource);

	/**
	 * @param e
	 */
	HttpResponse error(Throwable e);

	/**
	 * @return
	 */
	Charset charset();

	String header(String name);

	/**
	 * @return
	 */
	String contentsString();

	HttpResponse sendUncachedResource(Resource resource) throws IOException;

	HttpResponse sendCachedResource(Resource resource) throws IOException;

}