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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

import jj.util.GenericUtils;

/**
 * bridges tasks to execution models
 * 
 * @author jason
 *
 */
@Singleton
class Executors {
	
	private final Injector injector;
	
	private final ConcurrentMap<Class<?>, Object> executorInstances = new ConcurrentHashMap<>();

	@Inject
	Executors(Injector injector) {
		this.injector = injector;
	}
	
	<ExecutorType> void executeTask(JJTask<ExecutorType> task, Runnable runnable) {
		@SuppressWarnings("unchecked")
		ExecutorType executor = (ExecutorType)executorInstances.computeIfAbsent(task.getClass(), (t) -> {
			return injector.getInstance(GenericUtils.extractTypeParameterAsClass(t, JJTask.class, "ExecutorType"));
		});
		task.addRunnableToExecutor(executor, runnable);
	}
}
