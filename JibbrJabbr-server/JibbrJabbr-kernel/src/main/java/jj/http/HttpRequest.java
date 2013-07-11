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

import io.netty.handler.codec.http.HttpMethod;

import java.math.BigDecimal;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map.Entry;

import jj.DataStore;
import jj.jjmessage.JJMessage;
import jj.script.AssociatedScriptBundle;

/**
 * @author jason
 *
 */
public interface HttpRequest extends DataStore {

	HttpRequest data(String name, Object value);

	Object data(String name);

	boolean containsData(String name);

	Object removeData(String name);

	BigDecimal wallTime();

	AssociatedScriptBundle associatedScriptBundle();

	HttpRequest associatedScriptBundle(AssociatedScriptBundle associatedScriptBundle);

	HttpRequest startingInitialExecution();

	HttpRequest startingReadyFunction();

	HttpRequestState state();

	String host();

	boolean secure();

	URI absoluteUri();

	/**
	 * adds a message intended to be processed a framework startup
	 * on the client.  initially intended for event bindings but
	 * some other case may come up
	 * @param message
	 */
	HttpRequest addStartupJJMessage(JJMessage message);

	List<JJMessage> startupJJMessages();

	String toString();

	/**
	 * @return
	 */
	long timestamp();

	/**
	 * @return
	 */
	SocketAddress remoteAddress();

	/**
	 * @return
	 */
	String uri();

	/**
	 * @param ifNoneMatch
	 * @return
	 */
	boolean hasHeader(String ifNoneMatch);

	/**
	 * @param etag
	 * @return
	 */
	String header(String name);

	/**
	 * @return
	 */
	String id();

	/**
	 * @return
	 */
	String body();

	/**
	 * @return
	 */
	Charset charset();

	/**
	 * @return
	 */
	HttpMethod method();

	/**
	 * @return
	 */
	List<Entry<String, String>> allHeaders();

	/**
	 * @param userAgent
	 * @param userAgent2
	 */
	HttpRequest header(String name, String value);

}