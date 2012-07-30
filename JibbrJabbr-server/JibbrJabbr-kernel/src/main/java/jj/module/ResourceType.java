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
package jj.module;

import java.net.URI;
import java.util.HashMap;

import jj.html.HTMLFragment;

/**
 * Strategy for handling various resource types
 * 
 * @author jason
 *
 */
enum ResourceType {

	CLASS {
		@Override
		void processResource(Resource resource) {
			// load the class bytes as a class? might need a classloader reference
		}
		@Override
		boolean canBeServed() {
			// TODO Auto-generated method stub
			return false;
		}
	},
	HTML {
		@Override
		void processResource(Resource resource) {
			resource.htmlFragment = 
				new HTMLFragment(resource.charset.decode(resource.bytes).toString());
		}
	},
	PNG, // these two sit here for now
	CSS; // need to make a default type... octet or whatever.  it does nothing with it
	// just passes it along
	
	private static final HashMap<String, ResourceType> byName = new HashMap<>();
	
	static {
		for (ResourceType rt : values()) {
			byName.put(rt.toString().toLowerCase(), rt);
		}
	}
	
	static ResourceType fromURI(URI uri) {
		
		String uriS = uri.toString();
		int i = uriS.lastIndexOf('.') + 1;
		String ext = (i > 0 ? uriS.substring(i) : "").toLowerCase();
		return byName.get(ext);
	}
	
	/**
	 * do nothing by default? sure
	 * @param resource
	 */
	void processResource(Resource resource) {
		
	}
	
	// this is going to get more sophisticated, perhaps
	boolean canBeServed() {
		return true;
	}
}
