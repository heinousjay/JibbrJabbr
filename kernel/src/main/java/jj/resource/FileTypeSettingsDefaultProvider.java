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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * provides the default set of type configurations for
 * {@link ResourceConfiguration#fileTypeSettings()}
 * 
 * @author jason
 *
 */
@Singleton
public class FileTypeSettingsDefaultProvider implements Provider<Map<String, ResourceSettings>> {

// this class has to be public or the config system can't find it
// and the error is WEIRD, seriously not even comprehensible, since it's just
// an immutable set of data, it's fine
	
	private static final Map<String, ResourceSettings> settings;
	
	static {
		try (InputStream is = FileTypeSettingsDefaultProvider.class.getResourceAsStream("api/resource-configurations.json")) {
			
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Map<String, Object>> configuration =
				objectMapper.readValue(is, new TypeReference<Map<String, Map<String, Object>>>() {});
			
			HashMap<String, ResourceSettings> result = new HashMap<>(configuration.size() * 2);
			
			for (String ext : configuration.keySet()) {
				result.put(ext, makeSettings(configuration.get(ext)));
			}
			settings = Collections.unmodifiableMap(result);
			
		} catch (Exception e) {
			throw new AssertionError("trouble reading default resource configurations", e);
		}
	}
	
	private static ResourceSettings makeSettings(Map<String, Object> input) {
		String mimeType = (String)input.get("mimeType");
		Charset charset = input.containsKey("charset") ? Charset.forName((String)input.get("charset")) : null;
		boolean compressible = input.containsKey("compressible") && Boolean.TRUE.equals(input.get("compressible"));
		
		return new ResourceSettings(mimeType, charset, compressible);
 	}

	@Override
	public Map<String, ResourceSettings> get() {
		return settings;
	}

}
