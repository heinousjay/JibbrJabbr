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
package jj.http.server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import jj.resource.FileResource;

/**
 * <p>
 * Optional interface a {@link ServableResource} can implement
 * to indicate that serving should be done by transferring the
 * resource directly to the socket, if available
 * 
 * @author jason
 *
 */
public interface TransferableResource extends FileResource<Void>, ServableResource {
	
	FileChannel fileChannel() throws IOException;

	RandomAccessFile randomAccessFile() throws IOException;
}
