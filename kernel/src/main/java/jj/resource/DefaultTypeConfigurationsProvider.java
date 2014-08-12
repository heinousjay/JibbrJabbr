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
package jj.resource;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import jj.script.RhinoContext;

/**
 * provides the default set of type configurations for
 * {@link ResourceConfiguration#typeConfigurations()}
 * 
 * @author jason
 *
 */
@Singleton
public class DefaultTypeConfigurationsProvider implements Provider<Map<String, ResourceSettings>> {

// this class has to be public or the config system can't find it
// and the error is WEIRD, seriously not even comprehensible, since it's just
// an immutable set of data, it's fine
	
	private final Map<String, ResourceSettings> settings;
	
	@Inject
	DefaultTypeConfigurationsProvider(final Provider<RhinoContext> rhinoContextProvider) {
		HashMap<String, ResourceSettings> result = new HashMap<>();
		
		
		// need to read these from a file that the default-resource-properties module also uses
		// i guess JSON! then i can parse it out pretty trivially in both places.
		result.put("html", new ResourceSettings("text/html", UTF_8, true));
		result.put("js", new ResourceSettings("application/javascript", UTF_8, true));
		result.put("txt", new ResourceSettings("text/plain", UTF_8, true));
		result.put("properties", new ResourceSettings("text/plain", UTF_8, true));
		result.put("less", new ResourceSettings("text/plain", UTF_8, true));
		result.put("css", new ResourceSettings("text/css", UTF_8, true));
		result.put("jpg", new ResourceSettings("image/jpeg", null, false));
		result.put("gif", new ResourceSettings("image/gif", null, false));
		result.put("png", new ResourceSettings("image/png", null, false));
		
		
		settings = Collections.unmodifiableMap(result);
		
		try (RhinoContext context = rhinoContextProvider.get()) {
			// validating this is going to work cause Guice gets pissy
			// if your constructor does work, sometimes
		}
	}

	@Override
	public Map<String, ResourceSettings> get() {
		return settings;
	}

}
