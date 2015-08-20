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

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.TypeLiteral;

/**
 * @author jason
 *
 */
public class ExecutorBinder {
	
	private MapBinder<Class<?>, Object> executorBinder;

	public ExecutorBinder(final Binder binder) {
		executorBinder = MapBinder.newMapBinder(binder, new TypeLiteral<Class<?>>() {}, new TypeLiteral<Object>() {});
	}

	public void addExecutor(Class<?> executor) {
		assert executor.isAnnotationPresent(Singleton.class) : "executors must be singletons";
		executorBinder.addBinding(executor).to(executor);
	}
}
