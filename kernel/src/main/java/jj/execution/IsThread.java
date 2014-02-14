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
package jj.execution;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author jason
 *
 */
@Singleton
public class IsThread {

	final IOExecutor ioExecutor;
	final ServerExecutor serverExecutor;
	
	@Inject
	IsThread(final IOExecutor ioExecutor, final ServerExecutor serverExecutor) {
		this.ioExecutor = ioExecutor;
		this.serverExecutor = serverExecutor;
	}

	public boolean forIO() {
		return ioExecutor.isIOThread();
	}
	
	public boolean forServer() {
		return serverExecutor.isServerThread();
	}
}
