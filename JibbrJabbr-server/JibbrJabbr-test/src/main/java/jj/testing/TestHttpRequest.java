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

import javax.inject.Inject;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

import jj.http.JJHttpRequest;

/**
 * @author jason
 *
 */
class TestHttpRequest extends JJHttpRequest {
	
	/**
	 * @param request
	 * @param channel
	 */
	@Inject
	public TestHttpRequest(final FullHttpRequest request, final Channel channel) {
		super(request, channel);
	}

	@Override
	public String toString() {
		return new StringBuilder(TestHttpRequest.class.getSimpleName())
			.append("[").append(id()).append("] {")
			.append("method=").append(method())
			.append(", uri=").append(uri())
			.append(", headers=").append(request().headers())
			.append(", body=").append(body())
			.append("}")
			.toString();
	}

	/**
	 * @param uri
	 * @return
	 */
	public TestHttpRequest uri(String uri) {
		request().setUri(uri);
		return this;
	}
}
