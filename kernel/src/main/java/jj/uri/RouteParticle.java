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

/**
 * Represents a particle in a 
 * 
 * @author jason
 *
 */
interface RouteParticle {
	
	
	boolean matches(String part);

	/**
	 * @param particle
	 * @param result
	 */
	void populate(String particle, Map<String, String> result);

	/**
	 * @param contributions
	 * @param params
	 */
	boolean contribute(List<String> contributions, Map<String, String> params);
}
