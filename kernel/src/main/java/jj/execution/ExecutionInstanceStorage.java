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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Backs the execution instance to allow the task runner to associate things to threads
 * when related tasks are coming into scope
 * @author jason
 *
 */
@Singleton
class ExecutionInstanceStorage {

	private final ThreadLocal<Map<Object, Object>> bin = new ThreadLocal<Map<Object, Object>>() {
		protected Map<Object,Object> initialValue() {
			return new HashMap<>();
		}
	};
	
	@SuppressWarnings("unchecked")
	<T> T get(Class<?> type) {
		return (T)bin.get().get(type);
	}
	
	<T> void set(Class<?> type, T instance) {
		bin.get().put(type, instance);
	}
	
	void clear(Class<?> type) {
		bin.get().remove(type);
	}
	
	interface Handle {
		
		void resume();
	}
	
	Handle pause() {
		Map<Object, Object> map = new HashMap<>(bin.get());
		return () -> {
			bin.get().putAll(map);
		};
	}
}
