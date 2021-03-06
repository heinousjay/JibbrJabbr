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
package jj.resource;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import javax.inject.Inject;

import jj.util.SHA1Helper;

/**
 * represents a directory in the file structure of the app. right
 * now this is more bookkeeping than anything else, the idea being
 * that route resolution needs to know the directories without going
 * out to the file system from HTTP threads.  non-blocking ahoy
 * 
 * @author jason
 *
 */
public class DirectoryResource extends AbstractResource<Void> implements FileSystemResource {

	private final Path path;
	private final FileTime lastModified;
	private final String sha1;
	
	@Inject
	DirectoryResource(
		final Dependencies dependencies,
		final Path path
	) throws IOException {
		super(dependencies);
		
		BasicFileAttributes attributes;
		
		try {
			attributes = Files.readAttributes(path, BasicFileAttributes.class);
		} catch (NoSuchFileException nsfe) {
			throw new NoSuchResourceException(getClass(), path);
		}
		
		if (!attributes.isDirectory()) {
			throw new NoSuchResourceException(getClass(), path);
		}
		
		this.path = path;
		this.lastModified = attributes.lastModifiedTime();
		
		this.sha1 = SHA1Helper.keyFor(path.toString());
	}

	@Override
	public String sha1() {
		return sha1;
	}

	@Override
	public boolean needsReplacing() throws IOException {
		return (path.getFileSystem() == FileSystems.getDefault()) && lastModified.compareTo(Files.getLastModifiedTime(path)) < 0;
	}
	
	@Override
	public Path path() {
		return path;
	}
	
	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	protected boolean removeOnReload() {
		return false;
	}
}
