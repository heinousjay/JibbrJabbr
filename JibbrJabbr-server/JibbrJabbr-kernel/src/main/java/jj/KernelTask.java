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

/**
 * a basic runnable that renames its thread for debugging purposes and
 * provides interrupt handling and a cleanup method
 * 
 * @author Jason Miller
 *
 */
public abstract class KernelTask implements Runnable {
	
	protected final String taskName;
	
	protected KernelTask(final String taskName) {
		this.taskName = taskName;
	}
	
	public final void run() {
		String originalName = Thread.currentThread().getName();
		Thread.currentThread().setName(taskName);
		try {
			execute();
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			e.printStackTrace(); // log as kernel exception
		} finally {
			try {
				cleanup();
			} catch (Exception e) {
				// log this?
			}
			Thread.currentThread().setName(originalName);
		}
	}
	
	protected abstract void execute() throws Exception;
	
	protected void cleanup() {}
}

