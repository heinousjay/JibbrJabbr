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
package jj.logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provides default logging configuration
 * 
 * @author jason
 *
 */
@Singleton
public class LogLevelDefaultProvider implements Provider<Map<String, Level>> {
	
	private final Map<String, Level> levels;
	
	@Inject
	LogLevelDefaultProvider(@LoggerNames Map<String, String> loggerNames) {
		Map<String, Level> result = new HashMap<>();
		for (String logger : loggerNames.values()) {
			result.put(logger, Level.Info);
		}
		// special! don't activate the access log by default
		result.put("access", Level.Off);
		// or the netty stuff
		result.put("io.netty", Level.Off);
		// log the config at info by default
		result.put("script@config", Level.Info);
		levels = Collections.unmodifiableMap(result);
	}

	@Override
	public Map<String, Level> get() {
		return levels;
	}

	
}
