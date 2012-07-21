/*
` *    Copyright 2012 Jason Miller
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
package jj.io;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Reads a directory tree recursively starting at a given URI, returning a list of URIs of all
 * the files and directories from that starting URI, including the starting URI.
 * </p>
 * 
 * Say that better!
 * 
 * if the path identifies a jar, return the entries inside the jar
 * if it identifies a file, return the entries of the parent directory?
 * if it identified a directory... BOOM SHAKALAKA
 * 
 * also say that better!
 * 
 * @author jason
 *
 */
public abstract class DirectoryTreeRetriever extends FileSystemService.UriToPath {
	
	protected DirectoryTreeRetriever(final URI uri) {
		super(uri);
	}
	

	private final List<URI> uris = new ArrayList<>();
	
	private final class Walker extends SimpleFileVisitor<Path> {
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			uris.add(dir.toUri());
			return FileVisitResult.CONTINUE;
		}
		

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			uris.add(file.toUri());
			return FileVisitResult.CONTINUE;
		}
	}

	
	@Override
	void path(final Path path) {

		try {
			// if the path identifies a jar, return the entries inside the jar
			if (path.toString().endsWith(".jar")) {
				try (FileSystem jar = FileSystems.newFileSystem(path, null)) {
					Files.walkFileTree(jar.getPath("/"), new Walker());
				}
			// if it identified a directory... BOOM SHAKALAKA
			} else if (Files.isDirectory(path)) {
				Files.walkFileTree(path, new Walker());
			// otherwise use the parent
			// potential change, just return the incoming path? need to verify it exists
			} else {
				Files.walkFileTree(path.getParent(), new Walker());
				// if (Files.exists(path)) {
				// uris.add(path.toUri());
				// }
			}
		
			directoryTree(uris);
		} catch (Exception e) {
			failed(e);
		} finally {
			finished();
		}
	}
	
	/**
	 * 
	 * 
	 * <p>
	 * Do not perform any long running operations in this method as it runs on the
	 * FileSystemService thread.
	 * </p>
	 * 
	 * @param uris The List or URIs read from the directory.
	 */ 
	protected abstract void directoryTree(final List<URI> uris);
}
