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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * lil static object to encapsulate the base path for testing
 * 
 * @author jason
 *
 */
public class Base {
	
	public static final Path path;
	
	static {
		
		try {
			path = Paths.get(Base.class.getResource("/app/config.js").toURI()).getParent();
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	public static Path appPath() {
		return path;
	}
}
