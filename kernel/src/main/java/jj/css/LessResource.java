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
package jj.css;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import jj.resource.AbstractFileResource;

/**
 * <p>
 * represents a resource used to process a less script, including
 * the root script and any resources that get loaded along the way
 * 
 * <p>
 * This is mainly a way to hook into the dependency system so that
 * altering the dependencies reloads the containing StylesheetResource
 * @author jason
 *
 */
class LessResource extends AbstractFileResource<Void> {

	@Inject
	LessResource(Dependencies dependencies, Path path) {
		// don't keep the byte buffer around after processing it,
		// just load it again ourselves when it's needed
		super(dependencies, path, false);
	}
	
	String contents() {
		try {
			return new String(Files.readAllBytes(path), charset());
		} catch (Exception e) {
			throw new AssertionError("couldn't read the contents at " + path(), e);
		}
	}

}
