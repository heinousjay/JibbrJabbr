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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.webbitserver.helpers.DateHelper;
import org.webbitserver.stub.StubHttpRequest;

/**
 * @author jason
 *
 */
class TestHttpRequest extends StubHttpRequest {
	
	private final Map<String, String> headers = new HashMap<>();
	
	public Map<String, String> headers() {
		return Collections.unmodifiableMap(headers);
	}
	
	@Override 
	public String header(String name) {
		return headers.get(name);
	}

	@Override
	public StubHttpRequest header(String name, String value) {
		if (value == null) {
			headers.remove(name);
		} else {
			headers.put(name, value);
		}
		return this;
	}

	public StubHttpRequest header(String name, long value) {
		return header(name, String.valueOf(value));
	}

	public StubHttpRequest header(String name, Date value) {
		return header(name, DateHelper.rfc1123Format(value));
	}

	@Override
	public String toString() {
		return new StringBuilder(TestHttpRequest.class.getSimpleName())
			.append("[").append(id()).append("] {")
			.append("method=").append(method())
			.append(", uri=").append(uri())
			.append(", headers=").append(headers())
			.append(", body=").append(body())
			.append("}")
			.toString();
	}
}
