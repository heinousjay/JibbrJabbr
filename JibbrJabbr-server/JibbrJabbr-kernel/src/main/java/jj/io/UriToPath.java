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
package jj.io;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.api.Blocking;
import jj.io.FileSystemService.FileSystemAPI;

/**
 * Base class for FileSystemService API command objects that need to
 * translate a URI into a path.  which might be all of them?
 * 
 * could also be used to get the path and then redispatch, i guess, but that
 * seems a little pointless
 * @author jason
 *
 */
abstract class UriToPath extends FileSystemAPI {
	
	/**
	 * separates a jar URI into a path to the jar and a path inside the jar
	 * so that a file system instance can be determined
	 * probably just need to split on the !
	 */
	private static final Pattern JAR_URI_PARSER = Pattern.compile("^jar:([^!]+)!(.+)$");
	
	final URI uri;
	
	private volatile FileSystem openedFileSystem = null;
	
	UriToPath(final URI uri) {
		this.uri = uri;
		offer();
	}
	
	abstract void path(Path path);
	
	/**
	 * Call this when the API command is finished working with the returned
	 * path.
	 */
	void finished() {
		if (openedFileSystem != null) {
			try {
				openedFileSystem.close();
			} catch (IOException ioe) {
				
				// this should be logged!
			}
		}
		
		openedFileSystem = null;
	}
	
	@Blocking
	private Path filePath(URI uri) {
		try {
			return FileSystems.getDefault().getPath(uri.getPath());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Blocking
	private Path pathInJar(URI uri) {
		try {
			Matcher m = JAR_URI_PARSER.matcher(uri.toString());
			m.matches();
			String jarPath = m.group(1);
			String filePath = m.group(2);
			
			openedFileSystem = FileSystems.newFileSystem(filePath(URI.create(jarPath)), null);
			
			return openedFileSystem.getPath(filePath);
			
		} catch (Exception e) {
			return null;
		}
	}
	
	@Blocking
	private Path pathForURI(URI uri) {
		assert (uri != null) : "no URI provided";
		
		// figure out the filesystem it's in
		String scheme = uri.getScheme();
		if ("file".equals(scheme)) {
			return filePath(uri);
		} else if ("jar".equals(scheme)) {
			return pathInJar(uri);
		}
		
		return null;
	}
	
	@Override
	final void execute() {
		
		path(pathForURI(uri));
		
	}
}