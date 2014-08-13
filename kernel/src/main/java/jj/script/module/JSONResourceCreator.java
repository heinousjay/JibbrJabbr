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
package jj.script.module;

import javax.inject.Inject;
import javax.inject.Singleton;

import jj.resource.Location;
import jj.resource.SimpleResourceCreator;

/**
 * @author jason
 *
 */
@Singleton
class JSONResourceCreator extends SimpleResourceCreator<JSONResource> {

	@Inject
	JSONResourceCreator(jj.resource.SimpleResourceCreator.Dependencies dependencies) {
		super(dependencies);
	}
	
	@Override
	protected boolean arguments(Location base, String name) {
		return name.endsWith(".json");
	}
}
