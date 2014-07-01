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

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * resolves resources when not in the running system, ie tests
 * 
 * @author jason
 *
 */
public class BootstrapClassPath implements ResourceResolver {
	
	private final FileSystem jarFs;
	
	public BootstrapClassPath() {
		// in the integration tests run in the build, we are running in a jar, so we need to open that file system
		Path jar = JJ.jarForClass(getClass());
		if (jar != null) {
			try {
				jarFs = FileSystems.newFileSystem(jar, null);
			} catch (IOException e) {
				throw new AssertionError(e);
			}
		} else {
			jarFs = null;
		}
		
	}
	

	@Override
	public Path pathForFile(String file) throws Exception {
		return jarFs != null ?
			jarFs.getPath(file) :
			findPath(file);
	}

	private Path findPath(String file) throws Exception {
		URL url = BootstrapClassPath.class.getResource(file);
		return url != null ? Paths.get(url.toURI()) : null;
	}

}
