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
package jj.module;

import java.net.URI;
import java.util.List;

import net.jcip.annotations.Immutable;

import jj.KernelTask;
import jj.io.DirectoryTreeRetriever;

/**
 * <p>A module loaded from a jar file</p>
 * 
 * @author jason
 *
 */
@Immutable
class JarModule implements Module {
	
	private volatile boolean inService;
	
	JarModule(final URI uri) {
		
		new DirectoryTreeRetriever(uri) {

			@Override
			protected void directoryTree(final List<URI> uris) {
				inService = true;
			}
			
			@Override
			protected void failed(final Throwable t) {
				// not sure what to do... save it?
			}
		};
	}
	
	public boolean inService() {
		return inService;
	}
	
	private final class Worker extends KernelTask {

		/**
		 * @param taskName
		 */
		protected Worker(String taskName) {
			super(taskName);
		}

		@Override
		protected void execute() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	}
}
