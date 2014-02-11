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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import jj.Closer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ContinuationPending;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CurrentScriptEnvironmentTest {
	
	ContinuationPendingKey pendingKey;
	
	Continuable continuable = new Continuable() {
		
		ContinuationPendingKey pendingKey;
		
		@Override
		public ContinuationPendingKey pendingKey() {
			return pendingKey;
		}
		
		@Override
		public void pendingKey(ContinuationPendingKey pendingKey) {
			this.pendingKey = pendingKey;
		}
	};

	CurrentScriptEnvironment cse;
	
	RhinoContext rhinoContext;
	
	@Mock AbstractScriptEnvironment ase;
	
	@Mock ContinuationPending continuationPending;
	
	@Captor ArgumentCaptor<ContinuationState> continuationStateCaptor;
	
	@Before
	public void before() {
		
		pendingKey = new ContinuationPendingKey();
		
		MockRhinoContextProvider rcp = new MockRhinoContextProvider();
		rhinoContext = rcp.context;
		cse = new CurrentScriptEnvironment(rcp);
		
		continuable.pendingKey(null);
	}
	
	@After
	public void after() {
		assertThat(cse.current(), is(nullValue()));
	}
	
	@Test
	public void testCapturingContinuationInNormalScope() {
		
		given(rhinoContext.captureContinuation()).willReturn(continuationPending);
		given(ase.createContinuationContext(continuationPending)).willReturn(pendingKey);
		
		try (Closer closer = cse.enterScope(ase)) {
			
			cse.preparedContinuation(continuable);
			fail("should have thrown");
		} catch (ContinuationPending cp) {
			verify(cp).setApplicationState(continuationStateCaptor.capture());
			assertThat(continuationStateCaptor.getValue().pendingKey(), is(pendingKey));
		}
		
		verify(ase).createContinuationContext(continuationPending);
	}

}
