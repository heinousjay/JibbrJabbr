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
package jj.resource.stat.ic;

import jj.configuration.AppLocation;
import jj.configuration.PathResolver;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
public class StaticResourceMaker {
	public static StaticResource make(PathResolver app, ResourceInstanceCreator creator, AppLocation base, String name) throws Exception {
		return new StaticResourceCreator(app, creator).create(base, name);
	}
	
	public static StaticResourceCreator fake(PathResolver app) {
		return new StaticResourceCreator(app, null);
	}
}
