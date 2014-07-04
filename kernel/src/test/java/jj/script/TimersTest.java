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
import jj.execution.JJTask;
import jj.execution.MockTaskRunner;
import jj.util.Closer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TimersTest {
	
	MockTaskRunner taskRunner;
	@Mock ContinuationCoordinator continuationCoordinator;
	MockRhinoContextProvider contextProvider;
	Timers timers;
	
	@Mock Function function;
	@Mock ScriptableObject scope;
	@Mock ScriptEnvironment se;
	CurrentScriptEnvironment env;
	
	@Mock CancelKey cancelKey;

	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		env = new CurrentScriptEnvironment(contextProvider);
		timers = new Timers(taskRunner, continuationCoordinator, env);
	}

	@Test
	public void cancel() {
		timers.clearInterval.call(null, scope, null, new Object[] { cancelKey });
		
		verify(cancelKey).cancel();
	}
	
	@Test
	public void testSetInterval() throws Throwable {
		testRun(interval, 2L, true, 3L);
	}
	
	@Test
	public void testSetTimeout() throws Throwable {
		testRun(timeout, 600L, false, "hi", "there");
	}
	
	private Runnable interval = new Runnable() {
		
		@Override
		public void run() {
			timers.setInterval.call(null, scope, null, new Object[]{ function, "2", 3L });
		}
	};
	
	private Runnable timeout = new Runnable() {
		
		@Override
		public void run() {
			timers.setTimeout.call(null, scope, null, new Object[]{ function, 600, "hi", "there" });
		}
	};
	
	private void testRun(Runnable r, long delay, boolean repeat, Object...args) throws Throwable {
		try (Closer closer = env.enterScope(se)) {
			r.run();
		}
		assertThat(taskRunner.firstTaskDelay(), is(delay));
		
		JJTask task = taskRunner.runFirstTask();
		
		assertThat(taskRunner.taskWillRepeat(task), is(repeat));
		
		verify(continuationCoordinator).execute(se, function, args);
	}

}
