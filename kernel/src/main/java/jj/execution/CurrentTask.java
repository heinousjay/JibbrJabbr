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

import javax.inject.Singleton;

/**
 * Simple thread local container for the current task so the execution trace
 * can figure out what's what
 * 
 * @author jason
 *
 */
@Singleton
class CurrentTask {

	private final ThreadLocal<JJTask> store = new ThreadLocal<>();
	
	void set(final JJTask current) {
		store.set(current);
	}
	
	JJTask get() {
		return store.get();
	}

}
