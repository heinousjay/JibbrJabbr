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
package jj.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.conversion.Converters;
import jj.script.CurrentScriptEnvironment;

/**
 * server component used to implement the configuration API. it's
 * really just a set of hash maps and a facade to the conversion
 * system
 * 
 * @author jason
 *
 */
@Singleton
public class ConfigurationCollector {
	
	private final AtomicReference<Map<String, Object>> current = new AtomicReference<>();
	private LinkedHashMap<String, List<String>> errors = new LinkedHashMap<>(0);
	private HashMap<String, Object> inProgress = new HashMap<>();
	private final Converters converters;
	private final CurrentScriptEnvironment env;
	
	@Inject
	ConfigurationCollector(final Converters converters, final CurrentScriptEnvironment env) {
		this.converters = converters;
		this.env = env;
	}
	
	private void assertConfig() {
		assert isConfig() : "only available from a config script";
	}
	
	public boolean isConfig() {
		return env.currentRootScriptEnvironment() instanceof ConfigurationScriptEnvironment;
	}
	
	/**
	 * This is the interface for the API modules
	 * @param key
	 * @param value
	 */
	public void addConfigurationElement(String key, Object value) {
		assertConfig();
		inProgress.put(key, value);
	}
	
	public void addConfigurationMultiElement(String key, Object value) {
		assertConfig();
		if (!inProgress.containsKey(key)) {
			inProgress.put(key, new ArrayList<Object>());
		}
		@SuppressWarnings("unchecked")
		ArrayList<Object> list = ((ArrayList<Object>)inProgress.get(key));
		list.add(value);
	}
	
	public void addConfigurationMappedElement(String key, Object valueKey, Object valueValue) {
		assertConfig();
		if (!inProgress.containsKey(key)) {
			inProgress.put(key, new HashMap<Object, Object>());
		}
		@SuppressWarnings("unchecked")
		HashMap<Object, Object> map = ((HashMap<Object, Object>)inProgress.get(key));
		map.put(valueKey, valueValue);
	}
	
	public void accumulateError(String key, String error) {
		assertConfig();
		errors.computeIfAbsent(key, k -> {
			return new ArrayList<>(1);
		}).add(error);
	}
	
	<T> T get(String key, Class<T> type, Object defaultValue) {
		Map<String, Object> map = current.get();
		if (List.class.isAssignableFrom(type) && defaultValue == null) {
			defaultValue = Collections.EMPTY_LIST;
		}
		if (Map.class.isAssignableFrom(type) && defaultValue == null) {
			defaultValue = Collections.EMPTY_MAP;
		}
		return converters.convert(map != null && map.containsKey(key) ? map.get(key) : defaultValue, type);
	}
	
	ConfigurationErrored configurationComplete() {
		
		if (!errors.isEmpty()) {
			LinkedHashMap<String, List<String>> e = errors;
			errors = new LinkedHashMap<>();
			return new ConfigurationErrored(e);
		}
		
		for (String key : inProgress.keySet()) {
			if (inProgress.get(key) instanceof List) {
				List<?> list = Collections.unmodifiableList((List<?>)inProgress.get(key));
				inProgress.put(key, list);
			}
			if (inProgress.get(key) instanceof Map) {
				Map<?, ?> map = Collections.unmodifiableMap((Map<?, ?>)inProgress.get(key));
				inProgress.put(key, map);
			}
		}
		
		current.set(Collections.unmodifiableMap(inProgress));
		inProgress = new HashMap<>();
		
		return null;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(current = " + current + ")";
	}
}
