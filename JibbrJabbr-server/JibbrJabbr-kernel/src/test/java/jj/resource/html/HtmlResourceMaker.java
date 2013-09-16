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
package jj.resource.html;

import jj.configuration.Configuration;
import jj.resource.ResourceInstanceCreator;

/**
 * @author jason
 *
 */
public class HtmlResourceMaker {
	public static HtmlResource make(Configuration configuration, ResourceInstanceCreator creator, String baseName) throws Exception {
		return new HtmlResourceCreator(configuration, creator).create(baseName);
	}
	
	public static HtmlResourceCreator fake(Configuration configuration) {
		return new HtmlResourceCreator(configuration, null);
	}
}
