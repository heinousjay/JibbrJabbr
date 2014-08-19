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
package jj.resource.stat.ic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import javax.inject.Inject;

import jj.resource.AbstractFileResource;
import jj.resource.PathResolver;
import jj.resource.ResourceThread;
import jj.resource.TransferableResource;

/**
 * @author jason
 *
 */
public class StaticResource extends AbstractFileResource implements TransferableResource {
	
	private final boolean safeToServe;
	
	/**
	 * @param baseName
	 * @param path
	 * @throws IOException
	 */
	@Inject
	StaticResource(
		final Dependencies dependencies,
		final Path path,
		final PathResolver pathResolver
	) throws IOException {
		super(dependencies, path, false);
		safeToServe = base.internal() || pathResolver.pathInBase(path);
	}
	
	@Override
	public String contentType() {
		return settings.contentType();
	}
	
	@Override
	public boolean compressible() {
		return settings.compressible();
	}
	
	@Override
	@ResourceThread
	public FileChannel fileChannel() throws IOException {
		return FileChannel.open(path);
	}
	
	@Override
	@ResourceThread
	public RandomAccessFile randomAccessFile() throws IOException {
		return new RandomAccessFile(path.toFile(), "r");
	}

	@Override
	public String serverPath() {
		return "/" + sha1() + "/" + name();
	}
	
	@Override
	public boolean safeToServe() {
		return safeToServe;
	}
}
