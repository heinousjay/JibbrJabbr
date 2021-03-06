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
 * <p>
 * Locate the currently executing task instance, for example if you
 * need to register a promise.
 * 
 * @author jason
 *
 */
@Singleton
public class CurrentTask extends ExecutionInstance<JJTask<?>> {
	
	public String name() {
		JJTask<?> task = current();
		return task == null ? null : task.name();
	}
	
	public <T extends JJTask<?>> T currentAs(Class<T> type) {
		return type.cast(current());
	}

	public <T extends JJTask<?>> boolean currentIs(Class<T> type) {
		return type.isInstance(current());
	}
}
