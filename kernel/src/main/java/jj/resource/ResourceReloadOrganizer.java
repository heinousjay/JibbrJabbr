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

import java.util.HashSet;

/**
 * A helper to organize the reloading behavior - we just want to delete things, for the most part,
 * and only trigger reloads on objects that self-identify as roots
 * @author jason
 *
 */
class ResourceReloadOrganizer {
	
	final HashSet<AbstractResource> deletions = new HashSet<>();
	final HashSet<AbstractResource> reloads = new HashSet<>();
	
	ResourceReloadOrganizer(AbstractResource base, boolean delete) {
		
		placeResource(base, delete);
		
	}
	
	private void placeResource(AbstractResource resource, boolean delete) {
		// sorry for the javascripty java but the test passes!
		if ((delete || resource.removeOnReload() ? deletions : reloads).add(resource)) {
			// if we haven't seen this one before, traverse it
			for (AbstractResource dependent : resource.dependents()) {
				placeResource(dependent, false);
			}
		}
	}
	
	@Override
	public String toString() {
		return new StringBuilder("Removals: ").append(deletions).append("\n")
			.append("Reloads").append(reloads).append("\n")
			.toString();
	}
}