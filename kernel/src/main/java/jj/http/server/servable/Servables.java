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
package jj.http.server.servable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.http.server.uri.URIMatch;
import jj.resource.Resource;

/**
 * @author jason
 *
 */
@Singleton
public class Servables {

	private final Set<Servable<? extends Resource>> servables;
	
	@Inject
	Servables(final Set<Servable<? extends Resource>> servables) {
		this.servables = servables;
	}
	
	public List<Servable<? extends Resource>> findMatchingServables(final URIMatch uriMatch) {
		
		List<Servable<? extends Resource>> result = new ArrayList<>();
		
		for (final Servable<? extends Resource> servable : servables) {
			if (servable.isMatchingRequest(uriMatch)) {
				result.add(servable);
			}
		}
		
		return Collections.unmodifiableList(result);
	}

	/**
	 * @param uriMatch
	 * @return
	 */
	
	public Resource loadResource(final URIMatch uriMatch) {
		
		for (Servable<? extends Resource> servable : findMatchingServables(uriMatch)) {
			Resource resource = servable.loadResource(uriMatch);
			if (resource != null) {
				return resource;
			}
		}
		return null;
	}
}
