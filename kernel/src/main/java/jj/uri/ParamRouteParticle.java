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
package jj.uri;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.util.StringUtils;

/**
 * @author jason
 *
 */
class ParamRouteParticle implements RouteParticle {
	
	private static final Pattern PATTERN = Pattern.compile("\\{([\\w\\d]+)(?:=(.+))?\\}");
	
	private final String name;
	
	private final String defaultValue;
	
	ParamRouteParticle(final String configuration) {
		Matcher matcher = PATTERN.matcher(configuration);
		if (matcher.matches()) {
			name = matcher.group(1);
			defaultValue = matcher.group(2);
		} else {
			throw new IllegalArgumentException("param is not valid - " + configuration);
		}
	}

	@Override
	public boolean matches(String part) {
		
		return true;
	}
	
	@Override
	public void populate(String particle, Map<String, String> result) {
		result.put(name, StringUtils.isEmpty(particle) ? defaultValue : particle);
	}
	
	@Override
	public boolean contribute(List<String> contributions, Map<String, String> params) {
		String value = params.get(name);
		if (value != null && !value.equals(defaultValue)) {
			contributions.add(value);
		}
		return !StringUtils.isEmpty(value) || !StringUtils.isEmpty(defaultValue);
	}

	@Override
	public String toString() {
		return "parameter named " + name;
	}
}
