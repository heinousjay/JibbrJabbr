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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import jj.KernelTask;
import jj.SynchThreadPool;
import jj.io.DirectoryTreeRetriever;

/**
 * <p>A module loaded from a jar file</p>
 * 
 * this is effectively immutable, in that once constructed, it will either successfully
 * initialize and begin service, or it will fail somehow.  once it enters service, it
 * will work until discarded.  if initialization fails, it cannot be made useful.
 * 
 * we are slightly stretching the definition of immutable since everything happens
 * asynchronously.  some point after construction returns, you can find out if it's good
 * to go or not
 * 
 * @author jason
 *
 */
class Module {
	
	static final class Request {
		
	}
	
	private final BlockingQueue<Request> requestQueue = new LinkedTransferQueue<>();
	
	private volatile boolean inService = false;
	
	/**
	 * 
	 * @param uri
	 * @param threadPool
	 */
	Module(final URI uri, final SynchThreadPool threadPool) {
		assert uri != null : "uri was not provided";
		assert threadPool != null : "threadPool was not provided";
		
		new DirectoryTreeRetriever(uri) {
			
			@Override
			protected void directoryTree(final List<URI> uris) {
				threadPool.submit(new Worker("JarModule [" + uri +"]", uris));
			}
			
			@Override
			protected void failed(final Throwable t) {
				t.printStackTrace();
			}
		};
	}

	public boolean inService() {
		return inService;
	}
	
	private final class Worker extends KernelTask {
		
		/** The URIs to all the files inside the jar. */
		private final List<URI> uris;
		
		/**
		 * @param taskName
		 */
		protected Worker(final String taskName, final List<URI> uris) {
			super(taskName);
			this.uris = uris;
		}

		@Override
		protected void execute() throws Exception {
			
			final Resources resources = new Resources(uris);
			
			while (inService) {
				
				requestQueue.take();
				System.out.println(resources);
			}
		}
	}
}
