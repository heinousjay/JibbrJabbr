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

import jj.configuration.Location;
import jj.execution.Promise;

/**
 * <p>
 * Service component that asynchronously loads a resource 
 * 
 * @author jason
 *
 */
public interface ResourceLoader {

	/**
	 * <p>
	 * Asynchronously load the identified {@link Resource} using a {@link ResourceTask}
	 * 
	 * @param resourceClass The type of <code>Resource</code>
	 * @param base The {@link Location} of the <code>Resource</code>
	 * @param name The name of the <code>Resource</code>
	 * @param arguments The creation arguments of the <code>Resource</code>
	 * @return the <code>ResourceTask</code>'s {@link Promise}
	 */
	Promise loadResource(Class<? extends Resource> resourceClass, Location base, String name, Object... arguments);

}