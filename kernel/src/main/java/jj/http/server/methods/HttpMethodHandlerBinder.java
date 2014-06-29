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
package jj.http.server.methods;

import io.netty.handler.codec.http.HttpMethod;

import com.google.inject.Binder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

/**
 * @author jason
 *
 */
public class HttpMethodHandlerBinder {
	
	public interface With {
		ScopedBindingBuilder with(Class<? extends HttpMethodHandler> methodHandler);
	}

	private final MapBinder<HttpMethod, HttpMethodHandler> methodHandlerBinder;
	
	public HttpMethodHandlerBinder(Binder binder) {
		methodHandlerBinder = MapBinder.newMapBinder(binder, HttpMethod.class, HttpMethodHandler.class);
	}
	
	public With handle(final HttpMethod method) {
		return new With() {
			
			@Override
			public ScopedBindingBuilder with(Class<? extends HttpMethodHandler> methodHandler) {
				return methodHandlerBinder.addBinding(method).to(methodHandler);
			}
		};
	}
}
