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
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * 
 * manages the system jars that make up JibbrJabbr, exposes a simple
 * interface to get to their files and related information
 * 
 * @author jason
 *
 */
class SystemJars {
	
	private static final String META_INF = "/META-INF";
	private static final String MANIFEST_PATH = META_INF + "/MANIFEST.MF";

	private static final String JAR_GLOB = "*.jar";

	private final Path libPath;
	
	private static final class FileSystemNode {
		
		final FileSystem fileSystem;
		final Path jarPath;
		FileSystemNode next;
		
		FileSystemNode(final Path jarPath, final FileSystem fileSystem) throws IOException {
			this.jarPath = jarPath;
			this.fileSystem = fileSystem;
		}
	}
	
	private final Map<String, FileSystemNode> jars;
	
	private final Map<FileSystem, CodeSource> codeSources;
	
	SystemJars(final Path libPath) throws IOException {
		this.libPath = libPath;
		this.jars = makeJarsMap();
		this.codeSources = makeCodeSources();
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
	
	public CodeSource codeSourceForFile(String file) throws IOException {
		FileSystemNode fs = jarsForFile(file);
		Path attempt = null;
		
		while (attempt == null && fs != null) {
			attempt = fs.fileSystem.getPath(file);
			if (!Files.exists(attempt)) {
				attempt = null;
				fs = fs.next;
			}
		}
		
		return (fs != null) ? codeSources.get(fs.fileSystem) : null; 
	}
	
	public Manifest jarManifestForFile(String file) throws IOException {
		
		Manifest result = null;
		FileSystemNode fs = jarsForFile(file);
		if (fs != null) {
			Path manifest = fs.fileSystem.getPath(MANIFEST_PATH);
			if (Files.exists(manifest)) {
				try (InputStream is = Files.newInputStream(manifest)) {
					result = new Manifest(is);
				}
			}
		}
		
		return result;
	}
	
	private FileSystemNode jarsForFile(String file) {
		String key = Paths.get(file).getParent().toString() + "/";
		return jars.get(key);
	}
	
	private final DirectoryStream.Filter<Path> directoryFilter = new DirectoryStream.Filter<Path>() {

		@Override
		public boolean accept(Path entry) throws IOException {
			return Files.isDirectory(entry) && !entry.startsWith(META_INF);
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
	
	private void addDirectoriesAndRecurse(Path basePath, HashSet<Path> paths) throws IOException {
		try (DirectoryStream<Path> directories = Files.newDirectoryStream(basePath, directoryFilter)) {
			for (Path directory : directories) {
				Path nextDirectory = basePath.resolve(directory);
				if (hasFiles(nextDirectory)) {
					paths.add(nextDirectory);
				}
				addDirectoriesAndRecurse(nextDirectory, paths);
			}
		}
	}
	
	private Map<FileSystem, CodeSource> makeCodeSources() throws IOException {
		Map<FileSystem, CodeSource> result = new HashMap<>();
		
		for (FileSystemNode node : jars.values()) {
			while (node != null) {
				if (!result.containsKey(node.fileSystem)) {
					// should actually read the certs! if any
					CodeSource codeSource = new CodeSource(node.jarPath.toUri().toURL(), (Certificate[])null);
					result.put(node.fileSystem, codeSource);
				}
				node = node.next;
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
	
	private Map<String, FileSystemNode> makeJarsMap() throws IOException {
		
		Map<String, FileSystemNode> result = new HashMap<>();
		
		try (DirectoryStream<Path> libDir = Files.newDirectoryStream(libPath, JAR_GLOB)) {
			for (Path jarPath : libDir) {
				FileSystem myJarFS = FileSystems.newFileSystem(jarPath, null);
				HashSet<Path> paths = new HashSet<>();
				
				for (Path root : myJarFS.getRootDirectories()) {
					addDirectoriesAndRecurse(root, paths);
				}
				
				for (Path path : paths) {
					if (result.containsKey(path)) {
						result.get(path.toString()).next = new FileSystemNode(jarPath, myJarFS);
					} else {
						result.put(path.toString(), new FileSystemNode(jarPath, myJarFS));
					}
				}
			}
		}
		
		return Collections.unmodifiableMap(result);
	}
}
