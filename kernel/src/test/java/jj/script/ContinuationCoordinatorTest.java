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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.HashMap;
import java.util.Map;

import jj.event.Publisher;
import jj.jjmessage.JJMessage;
import jj.script.module.RequiredModule;
import jj.util.Closer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ConstProperties;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * this class is hard to test because it mainly
 * exercises rhino
 * 
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ContinuationCoordinatorTest {
	
	final String sha1 = "scriptsha1";
	
	ContinuationPendingKey pendingKey;
	
	@Mock(extraInterfaces = {ConstProperties.class}) Scriptable scope;
	
	RhinoContext context;
	
	@Mock Script script;
	
	@Mock AbstractScriptEnvironment scriptEnvironment;
	
	CurrentScriptEnvironment env = new CurrentScriptEnvironment(new MockRhinoContextProvider());
	
	@Mock ContinuationPending continuation;
	
	ContinuationState continuationState;
	
	@Mock Publisher publisher;
	
	@Mock Callable function;
	
	@Mock Script executedScript;

	ContinuationCoordinatorImpl continuationCoordinator;
	
	final Object[] args = { new Object(), new Object() };
	
	final String unexpectedExceptionString = "unexpected problem during script execution {}";
	
	@Mock ContinuationProcessor continuationProcessor1;
	
	@Mock ContinuationProcessor continuationProcessor2;
	
	@Mock ContinuationProcessor continuationProcessor3;
	
	@Mock ContinuationPendingCache cache;
	
	@Mock IsThread is;
	
	@Mock Closer closer;
	
	@Before
	public void before() {
		
		pendingKey = new ContinuationPendingKey();
		
		MockRhinoContextProvider contextProvider = new MockRhinoContextProvider();
		
		given(scriptEnvironment.script()).willReturn(script);
		given(scriptEnvironment.scope()).willReturn(scope);
		given(scriptEnvironment.sha1()).willReturn(sha1);
		
		continuationState = new ContinuationState(JJMessage.makeRetrieve(""));
		given(continuation.getApplicationState()).willReturn(continuationState);
		
		Map<Class<? extends Continuation>, ContinuationProcessor> continuationProcessors = new HashMap<>();
		//continuationProcessors.put(RestRequest.class, continuationProcessor1);
		continuationProcessors.put(JJMessage.class, continuationProcessor2);
		continuationProcessors.put(RequiredModule.class, continuationProcessor3);
		context = contextProvider.get();
		continuationCoordinator = new ContinuationCoordinatorImpl(contextProvider, env, publisher, continuationProcessors, cache, is);
		
		given(is.forScriptEnvironment(any(ScriptEnvironment.class))).willReturn(true);
	}
	
	@Test
	public void testFunctionExecutionNoContinuation() {
		
		ContinuationPendingKey result = continuationCoordinator.execute(scriptEnvironment, function, args[0], args[1]);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testFunctionExecutionWithContinuation() {
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(continuation);
		continuationState.continuationAs(JJMessage.class).pendingKey(pendingKey);
		
		ContinuationPendingKey result = continuationCoordinator.execute(scriptEnvironment, function, args);
		
		assertThat(result, is(notNullValue()));
		verify(continuationProcessor2).process(continuationState);
	}
	
	@Test
	public void testFunctionExecutionWithUnexpectedException() {
		
		final RuntimeException e = new RuntimeException();
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(e);
		
		try {
			continuationCoordinator.execute(scriptEnvironment, function);
			fail();
		} catch (RuntimeException re) {
			assertThat(re, is(sameInstance(e)));
		}
		
		verify(publisher).publish(isA(ScriptExecutionError.class));
	}
	
	@Test
	public void testScriptExecutionNoContinuation() {
		
		ContinuationPendingKey result = continuationCoordinator.execute(scriptEnvironment, executedScript);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testScriptExecutionWithContinuation() {
		
		given(context.executeScriptWithContinuations(executedScript, scope)).willThrow(continuation);
		continuationState.continuationAs(JJMessage.class).pendingKey(pendingKey);
		
		ContinuationPendingKey result = continuationCoordinator.execute(scriptEnvironment, executedScript);
		
		assertThat(result, is(notNullValue()));
		verify(continuationProcessor2).process(continuationState);
	}
	
	@Test
	public void testScriptExecutionWithUnexpectedException() {
		
		final RuntimeException e = new RuntimeException();
		
		given(context.executeScriptWithContinuations(executedScript, scope)).willThrow(e);
		
		try {
			continuationCoordinator.execute(scriptEnvironment, executedScript);
			fail();
		} catch (RuntimeException re) {
			assertThat(re, is(sameInstance(e)));
		}
		
		verify(publisher).publish(isA(ScriptExecutionError.class));
	}
	
	@Test
	public void testContinuationResumptionNoContinuation() {
		
		given(scriptEnvironment.continuationPending(pendingKey)).willReturn(continuation);
		given(scriptEnvironment.restoreContextForKey(pendingKey)).willReturn(closer);
		
		ContinuationPendingKey result = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, args);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testContinuationResumptionWithContinuation() {
		
		given(scriptEnvironment.continuationPending(pendingKey)).willReturn(continuation);
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(continuation);
		continuationState.continuationAs(JJMessage.class).pendingKey(pendingKey);
		
		ContinuationPendingKey result = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, args);
		
		assertThat(result, is(notNullValue()));
		verify(continuationProcessor2).process(continuationState);
	}
	
	@Test
	public void testContinuationResumptionWithUnexpectedException() {
		
		given(scriptEnvironment.continuationPending(pendingKey)).willReturn(continuation);
		given(scriptEnvironment.restoreContextForKey(pendingKey)).willReturn(closer);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(e);
		
		try {
			continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, args);
			fail();
		} catch (RuntimeException re) {
			assertThat(re, is(sameInstance(e)));
		}
		
		verify(publisher).publish(isA(ScriptExecutionError.class));
	}

}
