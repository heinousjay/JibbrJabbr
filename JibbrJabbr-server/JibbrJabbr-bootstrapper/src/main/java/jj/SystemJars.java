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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 
 * manages the system jars that make up JibbrJabbr, exposes a simple
 * interface to get to their files.  the motivation here is to make
 * class loading faster - the old style was way unoptimized and made
 * start-up take 7 seconds after JVM initialization.  now we're down
 * a bit on that :D
 * 
 * @author jason
 *
 */
class SystemJars {
	
	private static final String JAR_GLOB = "*.jar";

	private final Path libPath;
	
	private static final class FileSystemNode {
		
		final FileSystem fileSystem;
		FileSystemNode next;
		
		FileSystemNode(final FileSystem fileSystem) {
			this.fileSystem = fileSystem;
		}
	}
	
	private final Map<String, FileSystemNode> jars;
	
	SystemJars(final Path libPath) throws IOException {
		this.libPath = libPath;
		this.jars = makeJarsMap();
	}
	
	public Path pathForFile(String file) throws IOException {
		FileSystemNode fs = jarsForFile(file);
		
		Path attempt = null;
		
		while (attempt == null && fs != null) {
			attempt = fs.fileSystem.getPath(file);
			if (!Files.exists(attempt)) {
				attempt = null;
				fs = fs.next;
			}
		}
		
		return attempt;
	}
	
	public Path[] pathsForFile(String file) throws IOException {
		ArrayList<Path> result = new ArrayList<>();
		FileSystemNode fs = jarsForFile(file);
		while (fs != null) {
			Path attempt = fs.fileSystem.getPath(file);
			if (Files.exists(attempt)) {
				result.add(attempt);
			}
			fs = fs.next;
		}
		
		return result.toArray(new Path[result.size()]);
	}
	
	private FileSystemNode jarsForFile(String file) {
		String key = Paths.get(file).getParent().toString() + "/";
		return jars.get(key);
	}
	
	private final DirectoryStream.Filter<Path> directoryFilter = new DirectoryStream.Filter<Path>() {

		@Override
		public boolean accept(Path entry) throws IOException {
			return Files.isDirectory(entry) && !entry.startsWith("/META-INF");
		}
		
	};
	
	private boolean hasFiles(Path directory) throws IOException {
		boolean result = false;
		try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
			for (Path entry : entries) {
				result = result || Files.isRegularFile(entry);
			}
		}
		return result;
	}
	
	private void addDirectoriesAndRecurse(Path basePath, HashSet<String> paths) throws IOException {
		try (DirectoryStream<Path> directories = Files.newDirectoryStream(basePath, directoryFilter)) {
			for (Path directory : directories) {
				Path nextDirectory = basePath.resolve(directory);
				if (hasFiles(nextDirectory)) {
					paths.add(nextDirectory.toString());
				}
				addDirectoriesAndRecurse(nextDirectory, paths);
			}
		}
	}
	
	private Map<String, FileSystemNode> makeJarsMap() throws IOException {
		
		Map<String, FileSystemNode> result = new HashMap<>();
		
		try (DirectoryStream<Path> libDir = Files.newDirectoryStream(libPath, JAR_GLOB)) {
			for (Path jarPath : libDir) {
				FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null);
				HashSet<String> paths = new HashSet<>();
				
				for (Path root : myJarFS.getRootDirectories()) {
					addDirectoriesAndRecurse(root, paths);
				}
				
				for (String path : paths) {
					if (result.containsKey(path)) {
						result.get(path).next = new FileSystemNode(myJarFS);
					} else {
						result.put(path, new FileSystemNode(myJarFS));
					}
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
}
