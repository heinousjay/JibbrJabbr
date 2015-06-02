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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import jj.ServerStopping;
import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.DelayedExecutor;
import jj.execution.JJRejectedExecutionHandler;
import jj.execution.JJThreadFactory;
import jj.util.Clock;

/**
 * provides a single-threaded {@link DelayedExecutor} suitable for running script tasks.
 * 
 * @author jason
 *
 */
@Subscriber
class ScriptExecutor extends DelayedExecutor {
	
	private final JJThreadFactory threadFactory;

	@Inject
	ScriptExecutor(
		Clock clock,
		JJThreadFactory threadFactory,
		JJRejectedExecutionHandler handler
	) {
		super(
			clock,
			1, 1,
			10, MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),
			threadFactory.namePattern("JibbrJabbr Script Thread %s"),
			handler
		);
		this.threadFactory = threadFactory;
	}
	
	@Override
	protected String schedulerThreadName() {
		return Thread.currentThread().getName();
	}

	@Listener
	void on(ServerStopping event) {
		shutdown();
	}
	
	@Override
	protected boolean asynchronousScheduling() {
		return false;
	}

	public boolean isScriptThread() {
		return threadFactory.in();
	}
}
