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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.execution.DelayedExecutor.CancelKey;
import jj.execution.MockTaskRunner;
import jj.resource.ResourceKey;
import jj.script.module.RootScriptEnvironment;
import jj.util.Closer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TimersTest {
	
	MockTaskRunner taskRunner;
	MockRhinoContextProvider contextProvider;
	Timers timers;
	
	@Mock Callable callable;
	@Mock ScriptableObject scope;
	@Mock(extraInterfaces = {ChildScriptEnvironment.class}) AbstractScriptEnvironment module;
	@Mock(extraInterfaces = {RootScriptEnvironment.class}) AbstractScriptEnvironment root;
	@Mock ResourceKey rk;
	CurrentScriptEnvironment env;
	
	@Mock CancelKey cancelKey;
	@Mock ContinuationPendingKey pendingKey;

	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		taskRunner.cancelKey = cancelKey;
		given(((ChildScriptEnvironment)module).parent()).willReturn(root);
		env = new CurrentScriptEnvironment(contextProvider);
		given(root.cacheKey()).willReturn(rk);
		
		given(module.execute(any(Callable.class), anyVararg())).willReturn(pendingKey);
		
		timers = new Timers(taskRunner, env);
	}
	
	@Test
	public void testEnvironmentDied() {
		try (Closer closer = env.enterScope(root)) {
			timers.setInterval.call(null, scope, null, new Object[]{ callable, "2" });
		}
		
		ScriptEnvironmentDied event = new ScriptEnvironmentDied(root);
		timers.scriptEnvironmentDied(event);
		
		verify(cancelKey).cancel();
	}
	
	String cancelId;
	
	@Test
	public void testSetIntervalFromRoot() throws Throwable {
		testRun(interval1, 2L, true, 3L);
		testRun(interval2, 0L, true);
	}
	
	@Test
	public void testSetTimeoutFromRoot() throws Throwable {
		testRun(timeout1, 600L, false, "hi", "there");
		testRun(timeout2, 0L, false);
	}
	
	@Test
	public void testCancelSetInterval() throws Throwable {
		try (Closer closer = env.enterScope(module)) {
			interval1.run();
		}

		try (Closer closer = env.enterScope(root)) {
			timers.clearInterval.call(null, scope, null, new Object[] { cancelId });
		}
		
		verify(cancelKey).cancel();
	}
	
	@Test
	public void testCancelSetTimeout() throws Throwable {
		try (Closer closer = env.enterScope(module)) {
			timeout1.run();
		}
		
		try (Closer closer = env.enterScope(root)) {
			timers.clearTimeout.call(null, scope, null, new Object[] { cancelId });
		}
		
		verify(cancelKey).cancel();
	}
	
	private Runnable interval1 = new Runnable() {
		
		@Override
		public void run() {
			cancelId = (String)timers.setInterval.call(null, scope, null, new Object[]{ callable, "2", 3L });
		}
	};
	
	private Runnable interval2 = new Runnable() {
		
		@Override
		public void run() {
			cancelId = (String)timers.setInterval.call(null, scope, null, new Object[]{ callable });
		}
	};
	
	private Runnable timeout1 = new Runnable() {
		
		@Override
		public void run() {
			cancelId = (String)timers.setTimeout.call(null, scope, null, new Object[]{ callable, 600, "hi", "there" });
		}
	};
	
	private Runnable timeout2 = new Runnable() {
		
		@Override
		public void run() {
			cancelId = (String)timers.setTimeout.call(null, scope, null, new Object[]{ callable });
		}
	};
	
	private void testRun(Runnable r, long delay, boolean repeat, Object...args) throws Throwable {
		try (Closer closer = env.enterScope(module)) {
			r.run();
		}
		assertThat(taskRunner.firstTaskDelay(), is(delay));
		
		ScriptTask<?> task = (ScriptTask<?>)taskRunner.runFirstTask();
		
		// verify we're doing this! cause i forgot lol
		assertThat(task.pendingKey, is(pendingKey));
		
		task.complete();
		
		assertThat(taskRunner.taskWillRepeat(task), is(repeat));
		
		verify(module).execute(callable, args);
	}

}
