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
package jj;

import jj.logging.LoggedEvent;

import org.slf4j.Logger;

/**
 * @author jason
 *
 */
@ServerLogger
public class ServerStartingEvent implements LoggedEvent {
	
	private final Version version;
	
	ServerStartingEvent(Version version) {
		this.version = version;
	}

	@Override
	public void describeTo(Logger logger) {
		logger.info("Welcome to {} version {} commit {}", version.name(), version.version(), version.commitId());
		logger.info("Starting the server");
	}

}
