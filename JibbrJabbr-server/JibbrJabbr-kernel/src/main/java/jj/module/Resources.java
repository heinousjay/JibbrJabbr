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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import jj.io.FileBytesRetriever;

/**
 * A collection of Resource instances.  Handles processing 
 * Should be constructed inside a Module worker thread
 * once construction finishes
 * @author jason
 *
 */
class Resources {
	
	private final List<Resource> resourceList;
	
	private static final boolean isResource(final URI uri) {
		return !uri.toString().endsWith("/");
	}
	
	private static final List<Resource> makeResourceList(final List<URI> uris) {

		ArrayList<Resource> resourceList = new ArrayList<>();
		
		for (final URI uri : uris) {
			if (isResource(uri)) {
				resourceList.add(new Resource(uri));
			} 
		}
		
		return resourceList;
	}
	
	private static final void loadResourceBytes(final List<Resource> resourceList) throws Exception {
		
		final CountDownLatch latch = new CountDownLatch(resourceList.size());
		
		for (final Resource resource : resourceList) {
			new FileBytesRetriever(resource.uri) {
				
				@Override
				protected void bytes(ByteBuffer bytes) {
					resource.bytes = bytes.asReadOnlyBuffer();
					latch.countDown();
				}
				
				@Override
				protected void failed(Throwable t) {
					// need to report it outward somehow
					t.printStackTrace();
				}
			};
		}
		
		latch.await();
	}
	
	Resources(final List<URI> uris) throws Exception {
		
		resourceList = Collections.unmodifiableList(makeResourceList(uris));
		loadResourceBytes(resourceList);
	}
}
