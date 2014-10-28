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
package jj.http.client;

import jj.script.Continuation;
import jj.script.ContinuationPendingKey;

/**
 * 
 * 
 * @author jason
 *
 */
class HttpClientRequest implements Continuation {

	private final String method;
	private final String uri;

	private ContinuationPendingKey pendingKey;
	
	HttpClientRequest(final String method, final String uri) {
		this.method = method;
		this.uri = uri;
	}
	
	@Override
	public ContinuationPendingKey pendingKey() {
		return pendingKey;
	}
	
	@Override
	public void pendingKey(ContinuationPendingKey pendingKey) {
		assert this.pendingKey == null;
		assert pendingKey != null;
		this.pendingKey = pendingKey;
	}
	
	@Override
	public String toString() {
		return HttpClientRequest.class.getSimpleName() + "[method: " + method + ", uri: " + uri + "]";
	}

}
