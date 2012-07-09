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
public abstract class FileBytesRetriever extends FileSystemService.UriToPath {
	
	volatile boolean active = true;

	public FileBytesRetriever(final URI uri) {
		super(uri);
	}
	
	
	@Override
	final void path(final Path path) {
		// TODO Auto-generated method stub
		try {
			
			// probably need a better method
			callBytes(ByteBuffer.wrap(Files.readAllBytes(path)));
		} catch (Exception e) {
			callFailed(e);
		}
	}
	
	private void callBytes(final ByteBuffer bytes) {
		
		asyncThreadPool.submit(new Runnable() {
			
			@Override
			public void run() {
				bytes(bytes);
			}
		});
		
	}
	
	private void callFailed(final Throwable t) {
		
		asyncThreadPool.submit(new Runnable() {
			
			@Override
			public void run() {
				failed(t);
			}
		});
	}
	
	/**
	 * Called when the file is completely read.
	 * 
	 * Should probably also have a version that returns the channel
	 * for transfers?  different class?
	 * 
	 * @param bytes
	 */
	protected abstract void bytes(final ByteBuffer bytes);
	
	/**
	 * Called if the operation failed.  Might just pass along the exception?
	 */
	protected abstract void failed(final Throwable t);
}
