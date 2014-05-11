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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


@RunWith(MockitoJUnitRunner.class)
public class AbstractScriptEnvironmentTest {
	
	private class MyScriptEnvironment extends AbstractScriptEnvironment {
		
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
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String scriptName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String name() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String uri() {
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
	
	@Mock ContinuationPendingKey pendingKey;
	@Mock ContinuationPending continuationPending;
	
	@Mock ScriptableObject scope;


	@Before
	public void before() throws Exception {
		ase = new MyScriptEnvironment(dependencies = new MockAbstractScriptEnvironmentDependencies());
	}
	
	@Test
	public void testNewObject() {
		
		ase.newObject();
		
		verify(dependencies.rhinoContextProvider().context).newObject(scope);
	}
	
	@Test
	public void testExports() {
		
		ase.exports();
		
		verify(dependencies.rhinoContextProvider().context).evaluateString(scope, "module.exports", "evaluating exports");
	}

	@Test
	public void testPendingKey() {
		
		given(dependencies.pendingKeyProvider.get()).willReturn(pendingKey);
		
		assertThat(ase.createContinuationContext(continuationPending), is(pendingKey));
		
		assertThat(ase.continuationPending(pendingKey), is(continuationPending));
	}
	
	@Test
	public void testCreateChainedScope() {
		
		ScriptableObject chained = mock(ScriptableObject.class);
		given(dependencies.rhinoContextProvider().context.newObject(scope)).willReturn(chained);
		
		assertThat(ase.createChainedScope(scope), is(chained));
		
		verify(chained).setPrototype(scope);
		verify(chained).setParentScope(null);
	}
	
	@Test
	public void testConfigureInjectFunction() {
		
		assertThat(ase.configureInjectFunction(scope), is(scope));
		verify(scope).defineProperty(InjectFunction.NAME, dependencies.injectFunction, ScriptableObject.CONST);
		
		final String newName = "asSomethingElse";
		ase.configureInjectFunction(scope, newName);
		verify(scope).defineProperty(newName, dependencies.injectFunction, ScriptableObject.CONST);
	}
	
	@Mock ScriptableObject module;
	@Mock ScriptableObject exports;
	@Mock ScriptableObject require;
	
	@Test
	public void testConfigureModuleObjects() {
		
		final String moduleId = "moduleIdentifier";
		given(dependencies.rhinoContextProvider().context.newObject(scope)).willReturn(module, exports);
		given(dependencies.rhinoContextProvider().context.evaluateString(eq(scope), anyString(), eq("making require"))).willReturn(require);
		assertThat(ase.configureModuleObjects(moduleId, scope), is(scope));
		
		verify(scope).defineProperty("module", module, ScriptableObject.CONST);
		verify(module).defineProperty("id", moduleId, ScriptableObject.CONST);
		verify(module).defineProperty("exports", exports, ScriptableObject.EMPTY);
		verify(scope).defineProperty("exports", exports, ScriptableObject.CONST);
		verify(scope).defineProperty(eq("require"), eq(require), eq(ScriptableObject.CONST));
	}

}
