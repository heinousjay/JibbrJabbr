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
package jj.script;

import jj.configuration.Default;

/**
 * <p>
 * Allows configuring the script execution system.  for now,
 * this is really just how many threads to allow.  maybe
 * more will come to me
 * 
 * @author jason
 *
 */
public interface ScriptExecutionConfiguration {

	/**
	 * <p>
	 * Total number of threads to use for script execution.
	 * 
	 *  <p>
	 *  An individual script instance will be locked to a single
	 *  thread.  This item configures how many threads are available
	 *  for that execution.
	 *  
	 *  <p>
	 *  It's best to keep this at or under the number of physical cores
	 *  on the machine, I think.  need to measure that.  Definitely don't
	 *  exceed the number of logical cores, that TOTALLY misses the point
	 *  of this architecture
	 *  
	 *  <p>
	 *  lower bound is 1
	 * @return
	 */
	@Default("1")
	int threadCount();
}
