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

import static org.mockito.Mockito.mock;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 *
 */
public class MockTaskCreator extends TaskCreator {
	
	public MockTaskCreator() {
		super(mock(ExecutionTraceImpl.class));
	}
	
	private final Logger log = LoggerFactory.getLogger(MockTaskCreator.class);

	@Override
	public Runnable prepareTask(final JJRunnable task) {
		return new Runnable() {
			@Override
			public void run() {
				log.info("Beginning execution of {}", task);
				try {
					task.run();
				} catch (Exception e) {
					log.error("Exception executing a task", e);
					throw new RuntimeException(e);
				} catch (Throwable t) {
					log.error("Throwable executing a task", t);
					throw t;
				} finally {
					log.info("Ending execution of {}", task);
				}
			}
		};
	}
	
	@Override
	<T> RunnableFuture<T> newIOTask(Runnable runnable, T value) {
		return new FutureTask<T>(runnable, value);
	}
	
	@Override
	<V> RunnableScheduledFuture<V> newClientTask(Runnable runnable, RunnableScheduledFuture<V> task) {
		return task;
	}
	
	@Override
	<V> RunnableScheduledFuture<V> newHttpTask(Runnable runnable, RunnableScheduledFuture<V> task) {
		return task;
	}
	
	@Override
	<V> RunnableScheduledFuture<V> newScriptTask(Runnable runnable, RunnableScheduledFuture<V> task) {
		return task;
	}
}
