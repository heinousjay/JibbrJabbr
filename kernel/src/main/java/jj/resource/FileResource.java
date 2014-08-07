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

import java.nio.file.Path;

/**
 * <p>
 * Represents a {@link Resource} that lives in the filesystem
 * somewhere
 * 
 * <p>
 * Do not implement this directly, extend {@link AbstractFileResource}
 * 
 * @author jason
 *
 */
public interface FileResource extends Resource, ParentedResource {

	
	/**
	 * The path of the resource
	 * @return
	 */
	Path path();
	
	/**
	 * size of the resource in bytes
	 * @return
	 */
	long size();
}
