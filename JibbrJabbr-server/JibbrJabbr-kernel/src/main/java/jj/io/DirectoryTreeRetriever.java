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

import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Reads a directory tree recursively starting at a given URI, returning a list of URIs of all
 * the files and directories from that starting URI, not including the starting URI.
 * </p>
 * 
 * Say that better!
 * 
 * if the path identifies a jar, return the entries inside the jar
 * if it identifies a file, return the entries of the parent directory
 * if it identified a directory... BOOM SHAKALAKA
 * 
 * also say that better!
 * 
 * @author jason
 *
 */
public abstract class DirectoryTreeRetriever extends FileSystemService.UriToPath {
	
	protected DirectoryTreeRetriever(URI uri) {
		super(uri);
	}
	
	private void iteratePathAndAddToList(Path path, List<URI> uris) throws Exception {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
			for (Path entry : ds) {
				uris.add(entry.toUri());
				if (Files.isDirectory(entry)) {
					iteratePathAndAddToList(entry, uris);
				}
			}
		}
	}
	
	@Override
	void path(Path path) {
		// if the path identifies a jar, return the entries inside the jar
		// if it identifies a file, return the entries of the parent directory
		// if it identified a directory... BOOM SHAKALAKA
		try {
			List<URI> uris = new ArrayList<>();
			iteratePathAndAddToList(path, uris);
			callDirectoryTree(uris);
		} catch (Exception e) {
			callFailed(e);
		}
	}
	
	private void callDirectoryTree(final List<URI> uris) {
		
		asyncThreadPool.submit(new Runnable() {
			
			@Override
			public void run() {
				directoryTree(uris);
			}
		});
		
	}
	
	protected abstract void directoryTree(final List<URI> uris);
}
