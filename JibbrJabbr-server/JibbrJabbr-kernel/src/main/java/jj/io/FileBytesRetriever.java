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

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Instantiate a subclass with a URI to asynchronously retrieve 
 * the bytes of the file at that location
 * 
 * @author jason
 *
 */
public abstract class FileBytesRetriever extends UriToPath {
	
	volatile boolean active = true;

	public FileBytesRetriever(final URI uri) {
		super(uri);
	}
	
	
	@Override
	final void path(final Path path) {
		// TODO Auto-generated method stub
		try {
			
			// probably need a better method
			bytes(ByteBuffer.wrap(Files.readAllBytes(path)));
		} catch (Exception e) {
			failed(e);
		} finally {
			finished();
		}
	}
	
	/**
	 * <p>
	 * Called when the file is completely read with a ByteBuffer representing
	 * the contents.
	 * </p>
	 * 
	 * <p>
	 * Do not perform any long running operations in this method as it runs on the
	 * FileSystemService thread.
	 * </p>
	 * 
	 * @param bytes the ByteBuffer with the contents found at the constructed URI
	 */
	protected abstract void bytes(final ByteBuffer bytes);
}
