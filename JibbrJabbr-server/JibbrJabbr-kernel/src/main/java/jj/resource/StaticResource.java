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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import jj.execution.IOThread;

/**
 * @author jason
 *
 */
public class StaticResource extends AbstractFileResource implements TransferableResource {

	private final String mime;
	
	/**
	 * @param baseName
	 * @param path
	 * @throws IOException
	 */
	StaticResource(final ResourceCacheKey cacheKey, final Path path, final String baseName) throws IOException {
		super(cacheKey, baseName, path, false);
		mime = MimeTypes.get(baseName);
	}

	@Override
	public String uri() {
		return "/" + sha1 + "/" + baseName;
	}

	@Override
	public String mime() {
		return mime;
	}
	
	@Override
	@IOThread
	public FileChannel fileChannel() throws IOException {
		return FileChannel.open(path);
	}
	
	@Override
	@IOThread
	public RandomAccessFile randomAccessFile() throws IOException {
		return new RandomAccessFile(path.toFile(), "r");
	}
}
