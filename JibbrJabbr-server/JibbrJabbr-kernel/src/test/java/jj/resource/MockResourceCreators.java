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

import java.util.HashMap;

import jj.configuration.Configuration;

/**
 * @author jason
 *
 */
class MockResourceCreators {
	

	static StaticResourceCreator src;
	static HtmlResourceCreator hrc;

	static ResourceCreators realized(Configuration configuration) {
		src = new StaticResourceCreator(configuration, null);
		hrc = new HtmlResourceCreator(configuration, null);
		HashMap<Class<? extends Resource>, ResourceCreator<? extends Resource>> resourceCreators = new HashMap<>();
		resourceCreators.put(src.type(), src);
		resourceCreators.put(hrc.type(), hrc);
		return new ResourceCreators(resourceCreators);
	}
	
	private MockResourceCreators() {
		
	}

}
