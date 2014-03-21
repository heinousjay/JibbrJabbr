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

import jj.util.StringUtils;

/**
 * Represents a static particle in a URI
 * 
 * @author jason
 *
 */
class StaticRouteParticle implements RouteParticle {
	
	private final String particle;
	
	StaticRouteParticle(final String particle) {
		assert !StringUtils.isEmpty(particle);
		this.particle = particle;
	}
	
	@Override
	public boolean matches(String part) {
		return particle.equals(part);
	}
	
	@Override
	public void populate(String particle, Map<String, String> result) {
		// no
	}
	
	@Override
	public boolean contribute(List<String> contributions, Map<String, String> params) {
		contributions.add(particle);
		return true;
	}
	
	@Override
	public String toString() {
		return "path named " + particle;
	}
}
