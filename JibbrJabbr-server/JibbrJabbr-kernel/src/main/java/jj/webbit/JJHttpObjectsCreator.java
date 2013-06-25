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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

/**
 * The things we do for testing
 * @author jason
 *
 */
@Singleton
class JJHttpObjectsCreator {

	@Inject
	JJHttpObjectsCreator() {}
	
	JJHttpRequest createJJHttpRequest(final HttpRequest request) {
		if (request instanceof JJHttpRequest) {
			return (JJHttpRequest)request;
		}
		return new JJHttpRequest(request);
	}
	
	JJHttpResponse createJJHttpResponse(final JJHttpRequest request, final HttpResponse response) {
		if (response instanceof JJHttpResponse) {
			return (JJHttpResponse)response;
		}
		
		return new JJHttpResponse(request, response);
	}
}
