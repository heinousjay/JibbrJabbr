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
package jj;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Simple parent ClassLoader that only exposes the JibbrJabbr API
 * and the bootstrap.
 * 
 * needs to get the URL from elsewhere but it works
 * 
 * not really testable because the api jar is in the bootstrap classloader
 * during tests.
 * 
 * @author jason
 *
 */
public class APIClassLoader extends URLClassLoader {
	
	
	private static final URL[] urls;
	
	static {
		registerAsParallelCapable();
		try {
			urls = new URL[]{JJ.jarPath(JJ.uri(jj.api.Version.class)).toUri().toURL()};
		} catch (Exception e) {
			throw new AssertionError("Could not load the API.  Your installation is malformed.", e);
		}
	}
	
	
	public APIClassLoader() {
		
		super(urls, ClassLoader.getSystemClassLoader());
		
		// need to find the API jar path
		// open the jar filesystem
		// read the sonofabitch in
		// define all the classes ahead of time
		// and close yourself? sure
	}
}
