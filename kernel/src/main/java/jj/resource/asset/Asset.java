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
package jj.resource.asset;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import jj.JJ;

/**
 * @author jason
 *
 */
public enum Asset {
	
	;

	public static final Path jar = JJ.jarForClass(Asset.class);
	
	// our assets should be here
	public static final Path appPath;
	
	static {
		// just walking along to our assets directory
		Path attempt = null;
		try {
			if (jar == null) {
				// ugly but necessary for testing, and favicon.ico will always be there
				URI uri = Asset.class.getResource("/jj/assets/favicon.ico").toURI();
				attempt = Paths.get(uri).getParent();
			} 
			
		} catch (Exception e) {
			throw new AssertionError("couldn't locate internal assets, altering the jar?");
		}
		appPath = attempt;
	}
	
	public static Path path(String name) throws IOException {
		Path result = null;
		if (appPath != null) {
			result = appPath.resolve(name);
		} else {
			try (FileSystem myJarFS = FileSystems.newFileSystem(jar, null)) {
				result = myJarFS.getPath("/jj/assets/").resolve(name);
			} 
		}
		return result;
	}

}
