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
package jj.script.resource;

import jj.configuration.resolution.AppLocation;
import jj.configuration.resolution.PathResolver;
import jj.resource.ResourceInstanceCreator;
import jj.script.resource.ScriptResource;
import jj.script.resource.ScriptResourceCreator;

/**
 * @author jason
 *
 */
public class ScriptResourceMaker {

	/**
	 * @param app
	 * @param creator
	 * @param baseName
	 * @return
	 */
	public static ScriptResource make(PathResolver app, ResourceInstanceCreator creator, AppLocation base, String name) throws Exception {
		return new ScriptResourceCreator(app, creator).create(base, name);
	}

}
