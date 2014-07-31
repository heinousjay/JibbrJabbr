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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Encapsulates the map of resource types to the creator instance
 * 
 * @author jason
 *
 */
@Singleton
class ResourceCreators implements Iterable<SimpleResourceCreator<? extends AbstractResource>> {
	
	private final Map<Class<? extends AbstractResource>, SimpleResourceCreator<? extends AbstractResource>> resourceCreators;

	@Inject
	ResourceCreators(final Map<Class<? extends AbstractResource>, SimpleResourceCreator<? extends AbstractResource>> resourceCreators) {
		this.resourceCreators = Collections.unmodifiableMap(resourceCreators);
	}
	
	<T extends Resource> ResourceCreator<T> get(final Class<T> type) {

		@SuppressWarnings("unchecked")
		ResourceCreator<T> result = (ResourceCreator<T>)resourceCreators.get(type);
		
		assert(result != null) : "don't have a resource creator that can make " + type;
		
		assert(result.type() == type);
		
		return result;
	}

	@Override
	public Iterator<SimpleResourceCreator<? extends AbstractResource>> iterator() {
		return resourceCreators.values().iterator();
	}

	public List<String> knownResourceTypeNames() {
		List<String> result =
			new ArrayList<>(resourceCreators.keySet().stream().map(Class::getName).collect(Collectors.toList()));
		
		result.sort(null);
		
		return result;
	}
}
