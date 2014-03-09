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

import jj.configuration.AppLocation;

/**
 * @author jason
 *
 */
public class ResourceReloaded extends ResourceEvent {

	public ResourceReloaded(Class<? extends Resource> resourceClass, final AppLocation base, String name, Object...arguments) {
		super(resourceClass, base, name, arguments);
	}

	@Override
	protected String description() {
		return "resource reloaded";
	}
}
