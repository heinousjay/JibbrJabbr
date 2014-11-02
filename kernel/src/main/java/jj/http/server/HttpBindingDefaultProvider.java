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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class HttpBindingDefaultProvider implements Provider<List<Binding>> {
	
	public static final int DEFAULT_BINDING_PORT = 8080;

	private final HttpServerSwitch httpServerSwitch;
	
	@Inject
	HttpBindingDefaultProvider(final HttpServerSwitch httpServerSwitch) {
		this.httpServerSwitch = httpServerSwitch;
	}

	@Override
	public List<Binding> get() {
		
		List<Binding> result;
	
		final int overridePort = httpServerSwitch.port();
		if (overridePort > 1023 && overridePort < 65536) {
			result = Arrays.asList(new Binding(overridePort));
		} else {
			result = Collections.singletonList(new Binding(DEFAULT_BINDING_PORT));
		}
		
		return result;
	}

}
