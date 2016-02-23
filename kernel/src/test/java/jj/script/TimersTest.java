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
import jj.resource.Location;
import jj.resource.ResourceIdentifier;
import jj.script.module.RootScriptEnvironment;
import jj.util.Closer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TimersTest {

	public static class RootEnvironment extends AbstractScriptEnvironment<Void> implements RootScriptEnvironment<Void> {

		public RootEnvironment(Dependencies dependencies) {
			super(dependencies);
		}

		@Override
		public boolean needsReplacing() throws IOException {
			return false;
		}

		@Override
		public Scriptable scope() {
			return null;
		}

		@Override
		public Script script() {
			return null;
		}

		@Override
		public String scriptName() {
			return null;
		}

		@Override
		public String sha1() {
			return null;
		}

		@Override
		public ScriptableObject global() {
			return null;
		}

		@Override
		public Location moduleLocation() {
			return null;
		}
	}

	public static class ChildEnvironment extends AbstractScriptEnvironment<Void> implements ChildScriptEnvironment<Void> {

		public ChildEnvironment(Dependencies dependencies) {
			super(dependencies);
		}

		@Override
		public boolean needsReplacing() throws IOException {
			return false;
		}

		@Override
		public ScriptEnvironment<?> parent() {
			return null;
		}

		@Override
		public Scriptable scope() {
			return null;
		}

		@Override
		public Script script() {
			return null;
		}

		@Override
		public String scriptName() {
			return null;
		}

		@Override
		public String sha1() {
			return null;
		}
	}

	MockTaskRunner taskRunner;
	MockRhinoContextProvider contextProvider;
	Timers timers;
	
	@Mock Callable callable;
	@Mock ScriptableObject scope;
	@Mock ChildEnvironment child;
	@Mock ResourceIdentifier<ChildEnvironment, Void> childIdentifier;
	@Mock RootEnvironment root;
	@Mock ResourceIdentifier<RootEnvironment, Void> rootIdentifier;
	CurrentScriptEnvironment env;
	
	@Mock CancelKey cancelKey;
	@Mock PendingKey pendingKey;


	@SuppressWarnings("unchecked")
	private void prepIdentifiers() {
		given((ResourceIdentifier<RootEnvironment, Void>)root.identifier()).willReturn(rootIdentifier);
		given((ResourceIdentifier<ChildEnvironment, Void>) child.identifier()).willReturn(childIdentifier);
	}

	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		taskRunner.cancelKey = cancelKey;
		willReturn(root).given(((ChildScriptEnvironment<?>) child)).parent();
		env = new CurrentScriptEnvironment(contextProvider);
		
		given(child.execute(any(Callable.class), anyVararg())).willReturn(pendingKey);

		prepIdentifiers();
		
		timers = new Timers(taskRunner, env);
	}
	
	@Test
	public void testEnvironmentDied() {
		try (Closer closer = env.enterScope(root)) {
			timers.setInterval.call(null, scope, null, new Object[]{ callable, "2" });
		}
		
		ScriptEnvironmentDied event = new ScriptEnvironmentDied(root);
		timers.on(event);
		
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
		try (Closer closer = env.enterScope(child)) {
			interval1.run();
		}

		try (Closer closer = env.enterScope(root)) {
			timers.clearInterval.call(null, scope, null, new Object[] { cancelId });
		}
		
		verify(cancelKey).cancel();
	}
	
	@Test
	public void testCancelSetTimeout() throws Throwable {
		try (Closer closer = env.enterScope(child)) {
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
		try (Closer closer = env.enterScope(child)) {
			r.run();
		}
		assertThat(taskRunner.firstTaskDelay(), is(delay));
		
		ScriptTask<?> task = (ScriptTask<?>)taskRunner.runFirstTask();
		
		// verify we're doing this! cause i forgot lol
		assertThat(task.pendingKey, is(pendingKey));
		
		task.complete();
		
		assertThat(taskRunner.taskWillRepeat(task), is(repeat));
		
		verify(child).execute(callable, args);
	}

}
