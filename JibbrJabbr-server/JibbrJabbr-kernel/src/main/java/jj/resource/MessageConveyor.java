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

import jj.api.NonBlocking;


/**
 * A takeoff on the cal10n project, tying things a little
 * more closely to the enumeration and not reloading bundles
 * behind the scenes.  Using JDK 1.6 facilities for loading
 * properties in different encodings.
 * 
 * @author jason
 *
 */
interface MessageConveyor<E extends Enum<E>> {

	/**
	 * Retrieve an appropriate message for the given key and optional parameters.  
	 * Implementations of this method should not block
	 * @param key
	 * @param args
	 * @return
	 */
	@NonBlocking
	String getMessage(final E key, final Object... args);

}