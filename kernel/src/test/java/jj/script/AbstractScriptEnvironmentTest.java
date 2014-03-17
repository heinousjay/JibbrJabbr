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
import javax.inject.Provider;

import jj.resource.ResourceCacheKey;
import jj.script.AbstractScriptEnvironment.Dependencies;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;


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
	
	@Mock ResourceCacheKey cacheKey;
	@Mock Provider<RhinoContext> contextProvider;
	@Mock Provider<ContinuationPendingKey> pendingKeyProvider;
	@Mock RequireInnerFunction makeRequireFunction;
	
	MyScriptEnvironment ase;
	
	@Mock ContinuationPendingKey pendingKey;
	@Mock ContinuationPending continuationPending;
	
	@Mock Scriptable scope;
	@Mock RhinoContext context;

	@Before
	public void before() throws Exception {
		ase = new MyScriptEnvironment(
			new Dependencies(cacheKey, contextProvider, pendingKeyProvider, makeRequireFunction)
		);
	}
	
	@Test
	public void testNewObject() {
		
		given(contextProvider.get()).willReturn(context);
		
		ase.newObject();
		
		verify(context).newObject(scope);
	}
	
	@Test
	public void testExports() {
		
		given(contextProvider.get()).willReturn(context);
		
		ase.exports();
		
		verify(context).evaluateString(scope, "module.exports", "evaluating exports");
	}

	@Test
	public void testPendingKey() {
		
		given(pendingKeyProvider.get()).willReturn(pendingKey);
		
		assertThat(ase.createContinuationContext(continuationPending), is(pendingKey));
		
		assertThat(ase.continuationPending(pendingKey), is(continuationPending));
	}
	
	@Test
	public void testCreateChainedScope() {
		
	}

}
