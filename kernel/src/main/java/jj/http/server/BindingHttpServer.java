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

import org.slf4j.Logger;

import jj.ServerLogger;
import jj.logging.LoggedEvent;

/**
 * @author jason
 *
 */
@ServerLogger
class BindingHttpServer extends LoggedEvent {
	
	private final Binding binding;
	
	BindingHttpServer(Binding binding) {
		this.binding = binding;
	}

	@Override
	public void describeTo(Logger logger) {
		if (binding.host() == null) {
			logger.info("Binding to {}", binding.port());
		} else {
			logger.info("Binding to {}:{}", binding.host(), binding.port());
		}
	}

}
