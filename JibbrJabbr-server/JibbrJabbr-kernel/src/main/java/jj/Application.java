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
package jj;

import java.io.Closeable;
import java.net.URL;

import jj.api.Blocking;
import jj.api.NonBlocking;

import net.jcip.annotations.ThreadSafe;

/**
 * Should this be immutable, with new instances created when things change?
 * 
 * Represents some level of Application containment,
 * coordinating the set of resources that make it up
 * - picocontainer
 * - kernel connector
 * - classloader
 * - filesystem path
 * - library jars
 * - sub applications
 * 
 * Primary responsibility is to mediate between the app
 * and kernel services, which basically is to say all
 * i/o runs through the kernel.  This class and anything
 * it controls should never block.
 * 
 * The root Application in a given hierarchy establishes
 * the thread pool for that application, and is responsible
 * for creating and maintaining its own children.
 * 
 * @author Jason Miller
 *
 */
@ThreadSafe
public class Application implements Closeable {

	protected final URL baseURL;
	private volatile boolean closed = false;
	private volatile boolean loaded = false;
	
	public Application(URL baseURL) throws Exception {
		assert (baseURL != null) : "baseURL is required";
		
		this.baseURL = baseURL;
		load();
	}
	
	protected void load() throws Exception {
		;
	}
	
	@NonBlocking
	public boolean loaded() {
		return loaded;
	}
	
	@Blocking
	public boolean respond(String path) {
		return true;
	}

	@Override
	public void close() {
		this.closed = true;
	}
	
	public boolean closed() {
		return closed;
	}
	
}
