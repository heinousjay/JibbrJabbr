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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


@RunWith(MockitoJUnitRunner.class)
public class AbstractScriptEnvironmentTest {
	
	private class MyScriptEnvironment extends AbstractScriptEnvironment<Void> {
		
		protected MyScriptEnvironment(Dependencies dependencies) {
			super(dependencies);
		}

		@Override
		public Scriptable scope() {
			// TODO Auto-generated method stub
			return scope;
		}
		
		@Override
		public Script script() {
			return script;
		}
		
		@Override
		public String scriptName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String sha1() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean needsReplacing() throws IOException {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	MockAbstractScriptEnvironmentDependencies dependencies;
	
	MyScriptEnvironment ase;
	
	@Mock PendingKey pendingKey;
	@Mock ContinuationPending continuationPending;
	
	@Mock ScriptableObject scope;
	@Mock Script script;


	@Before
	public void before() throws Exception {
		ase = new MyScriptEnvironment(dependencies = new MockAbstractScriptEnvironmentDependencies(MyScriptEnvironment.class, "test environment"));
	}
	
	@Test
	public void testBirth() {
		
		// when
		ase.beginInitializing();
		
		// then
		verify(dependencies.continuationCoordinator()).execute(ase, script);
		assertTrue(ase.initializing());
	}
	
	@Test
	public void testDeath() {
		// given
		given(dependencies.scriptEnvironmentDependencies.pendingKeyProvider.get()).willReturn(pendingKey);
		ase.createContinuationContext(continuationPending);
		
		// when
		ase.died();
		
		// then
		verify(dependencies.scriptEnvironmentDependencies.continuationPendingCache).removePendingTasks(new HashSet<>(Arrays.asList(pendingKey)));
		assertThat(dependencies.publisher().events.size(), is(1));
		assertTrue(dependencies.publisher().events.get(0) instanceof ScriptEnvironmentDied);
		ScriptEnvironmentDied event = (ScriptEnvironmentDied)dependencies.publisher().events.get(0);
		assertThat(event.scriptEnvironment(), is(ase));
	}
	
	@Test
	public void testScriptExecution() {
		
		// when
		ase.execute(script);
		
		// then
		verify(dependencies.continuationCoordinator()).execute(ase, script);
	}
	
	@Test
	public void testCallableExecution() {
		
		// given
		Callable callable = mock(Callable.class);
		Object[] args = new Object[0];
		
		// when
		ase.execute(callable, args);
		
		// then
		verify(dependencies.continuationCoordinator()).execute(ase, callable, args);
	}
	
	@Test
	public void testNewObject() {
		
		ase.newObject();
		
		verify(dependencies.mockRhinoContextProvider().context).newObject(scope);
	}
	
	@Test
	public void testExports() {
		
		ase.exports();
		
		verify(dependencies.mockRhinoContextProvider().context).evaluateString(scope, "module.exports", "evaluating exports");
	}

	@Test
	public void testPendingKey() {
		
		// given
		given(dependencies.scriptEnvironmentDependencies.pendingKeyProvider.get()).willReturn(pendingKey);
		
		// when
		PendingKey newKey = ase.createContinuationContext(continuationPending);
		
		// then
		assertThat(newKey, is(pendingKey));
		
		assertThat(ase.continuationPending(pendingKey), is(continuationPending));
	}
	
	@Test
	public void testCreateChainedScope() {
		
		ScriptableObject chained = mock(ScriptableObject.class);
		given(dependencies.mockRhinoContextProvider().context.newChainedScope(scope)).willReturn(chained);
		
		assertThat(ase.createChainedScope(scope), is(chained));
	}
	
	@Test
	public void testConfigureInjectFunction() {
		
		assertThat(ase.configureInjectFunction(scope), is(scope));
		verify(scope).defineProperty(InjectFunction.NAME, dependencies.scriptEnvironmentDependencies.injectFunction, ScriptableObject.CONST);
		
		final String newName = "asSomethingElse";
		ase.configureInjectFunction(scope, newName);
		verify(scope).defineProperty(newName, dependencies.scriptEnvironmentDependencies.injectFunction, ScriptableObject.CONST);
	}
	
	@Mock ScriptableObject module;
	@Mock ScriptableObject exports;
	@Mock ScriptableObject require;
	
	@Test
	public void testConfigureModuleObjects() {
		
		final String moduleId = "moduleIdentifier";
		given(dependencies.mockRhinoContextProvider().context.newObject(scope)).willReturn(module, exports);
		given(dependencies.mockRhinoContextProvider().context.evaluateString(
			eq(scope),
			anyString(),
			eq(AbstractScriptEnvironment.class.getSimpleName() + " require function definition")
		)).willReturn(require);
		assertThat(ase.configureModuleObjects(moduleId, scope), is(scope));
		
		verify(scope).defineProperty("module", module, ScriptableObject.CONST);
		verify(module).defineProperty("id", moduleId, ScriptableObject.CONST);
		verify(module).defineProperty("exports", exports, ScriptableObject.EMPTY);
		verify(scope).defineProperty("exports", exports, ScriptableObject.CONST);
		verify(scope).defineProperty(eq("require"), eq(require), eq(ScriptableObject.CONST));
	}

}
