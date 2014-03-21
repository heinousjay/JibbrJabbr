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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jj.util.StringUtils;

/**
 * represents an incoming URI
 * 
 * @author jason
 *
 */
public class Route {
	
	private static final Pattern PATH_SPLITTER = Pattern.compile("(?=/)");
	
	private static final Pattern PARAM_READ_SPLITTER = Pattern.compile("/");
	
	private final List<RouteParticle> routeParticles;
	
	public Route(final String configuration) {
		validateInput(configuration);
		
		this.routeParticles = makeRouteParticles(configuration);
	}
	
	private List<RouteParticle> makeRouteParticles(final String configuration) {
		List<RouteParticle> routeParticles = new ArrayList<>();
		String[] particles = PATH_SPLITTER.split(configuration);
		for (String particle : particles) {
			if (!StringUtils.isEmpty(particle)) {
				routeParticles.add(createRouteParticle(particle.substring(1)));
			}
		}
		
		return Collections.unmodifiableList(routeParticles);
	}
	
	
	/**
	 * @param particle
	 * @return
	 */
	private RouteParticle createRouteParticle(String particle) {
		RouteParticle routeParticle = null;
		if ("".equals(particle)) {
			routeParticle = new PathSeparatorEndCapRouteParticle();
		} else if (particle.startsWith("{") && particle.endsWith("}")) {
			routeParticle = new ParamRouteParticle(particle);
		} else {
			routeParticle = new StaticRouteParticle(particle);
		}
		
		return routeParticle;
	}


	private void validateInput(final String configuration) {
		if (StringUtils.isEmpty(configuration) || !configuration.startsWith("/")) {
			throw new IllegalArgumentException("route configuration is not properly specified");
		}
	}
	
	public boolean matches(final String uri) {
		boolean matches = true;
		Iterator<RouteParticle> routeParticleIterator = routeParticles.iterator();
		for (String particle : PATH_SPLITTER.split(uri.substring(1))) {
			matches = matches && routeParticleIterator.hasNext() && routeParticleIterator.next().matches(particle);
		}
		
		while (matches && routeParticleIterator.hasNext()) {
			matches = routeParticleIterator.next().matches("");
		}
		
		return matches;
	}
	
	private static class ArrayIterator<T> implements Iterator<T> {
		
		private final T[] array;
		private int index = 0;
		
		ArrayIterator(T[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public T next() {
			return array[index++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public Map<String, String> readParams(final String uri) {
		HashMap<String, String> result = new HashMap<>();
		
		Iterator<String> uriIterator = new ArrayIterator<String>(PARAM_READ_SPLITTER.split(uri.substring(1)));

		for (RouteParticle routeParticle : routeParticles) {
			String particle = uriIterator.hasNext() ? uriIterator.next() : "";
			routeParticle.populate(particle, result);
		}
		
		return result;
	}
	
	public String generate(final Map<String, String> params) {
		List<String> contributions = new ArrayList<>();
		boolean valid = true;
		for (RouteParticle routeParticle : routeParticles) {
			valid = valid && routeParticle.contribute(contributions, params);
		}
		
		StringBuilder result = new StringBuilder();
		if (valid) {
			for (String contribution : contributions) {
				result.append("/").append(contribution);
			}
		}
		return result.toString();
	}
	
	@Override
	public String toString() {
		return routeParticles.toString();
	}
}
