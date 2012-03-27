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

import java.nio.channels.CompletionHandler;
import java.util.concurrent.FutureTask;

/**
 * A two-part task to process some sort of potentially blocking
 * service, like file i/o.  Basic concept is the first task is the
 * i/o itself, second task is what to do with the result.
 * 
 * Probably needs to be not tied to the kernel pool, since there will
 * likely be a separate thread pool for non-HTTP I/O and a thread pool
 * for app tasks.
 * 
 * @author Jason Miller
 *
 */
public class KernelTask<T> extends FutureTask<T> {
	
	public KernelTask(Runnable task, CompletionHandler<?, ?> completion) {
		super(task, null);
		
	}
	
	@Override
	protected void done() {
		// gah gah gah gah gah
	}
}

