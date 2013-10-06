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
package jj.resource.script;

import jj.configuration.Configuration;
import jj.resource.ResourceInstanceCreator;
import jj.resource.script.ScriptResource;
import jj.resource.script.ScriptResourceCreator;

/**
 * @author jason
 *
 */
public class ScriptResourceMaker {

	/**
	 * @param configuration
	 * @param creator
	 * @param baseName
	 * @return
	 */
	public static ScriptResource make(Configuration configuration, ResourceInstanceCreator creator, String baseName) throws Exception {
		return new ScriptResourceCreator(configuration, creator).create(baseName);
	}

}
