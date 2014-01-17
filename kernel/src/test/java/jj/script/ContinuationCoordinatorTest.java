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
import java.util.HashMap;
import java.util.Map;

import jj.resource.document.DocumentScriptEnvironment;

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
import org.slf4j.Logger;

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
	
	final String pendingKey = "pendingKey";
	
	@Mock(extraInterfaces = {ConstProperties.class}) Scriptable scope;
	
	RhinoContext context;
	
	@Mock Script script;
	
	@Mock ScriptEnvironment scriptEnvironment;
	
	@Mock DocumentScriptEnvironment documentScriptEnvironment;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ContinuationPending continuation;
	
	@Mock ContinuationState continuationState;
	
	@Mock Logger logger;
	
	@Mock Callable function;

	ContinuationCoordinator continuationCoordinator;
	
	final Object[] args = { new Object(), new Object() };
	
	final String unexpectedExceptionString = "unexpected problem during script execution {}";
	
	@Mock ContinuationProcessor continuationProcessor1;
	
	@Mock ContinuationProcessor continuationProcessor2;
	
	@Mock ContinuationProcessor continuationProcessor3;
	
	@Before
	public void before() {
		
		given(scriptEnvironment.script()).willReturn(script);
		given(scriptEnvironment.scope()).willReturn(scope);
		given(scriptEnvironment.sha1()).willReturn(sha1);
		
		given(documentScriptEnvironment.script()).willReturn(script);
		given(documentScriptEnvironment.scope()).willReturn(scope);
		given(documentScriptEnvironment.sha1()).willReturn(sha1);
		
		given(continuation.getApplicationState()).willReturn(continuationState);
		
		Map<ContinuationType, ContinuationProcessor> continuationProcessors = new HashMap<>();
		continuationProcessors.put(ContinuationType.AsyncHttpRequest, continuationProcessor1);
		continuationProcessors.put(ContinuationType.JJMessage, continuationProcessor2);
		continuationProcessors.put(ContinuationType.RequiredModule, continuationProcessor3);
		
		MockRhinoContextProvider contextProvider = new MockRhinoContextProvider();
		context = contextProvider.get();
		continuationCoordinator = new ContinuationCoordinator(contextProvider, currentScriptContext, logger, continuationProcessors);
	}
	
	@Test
	public void testInitialExecutionNoContinuation() {
		
		boolean result = continuationCoordinator.execute(scriptEnvironment);
		
		verify((ConstProperties)scope).putConst("scriptKey", scope, sha1);
		
		assertThat(result, is(true));
	}
	
	@Test
	public void testInitialExecutionWithContinuation() {
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(continuation);
		given(continuationState.type()).willReturn(ContinuationType.AsyncHttpRequest);
		
		boolean result = continuationCoordinator.execute(scriptEnvironment);

		assertThat(result, is(false));
		verify(continuationProcessor1).process(continuationState);
	}
	
	@Test
	public void testInitialExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(e);
		
		continuationCoordinator.execute(scriptEnvironment);
		
		verify(logger).error(unexpectedExceptionString, scriptEnvironment);
		verify(logger).error("", e);
	}
	
	@Test
	public void testFunctionExecutionNoContinuation() {
		
		boolean result = continuationCoordinator.execute(documentScriptEnvironment, function, args[0], args[1]);
		
		assertThat(result, is(true));
	}
	
	@Test
	public void testFunctionExecutionWithContinuation() {
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(continuation);
		given(continuationState.type()).willReturn(ContinuationType.JJMessage);
		
		boolean result = continuationCoordinator.execute(documentScriptEnvironment, function, args);
		
		assertThat(result, is(false));
		verify(continuationProcessor2).process(continuationState);
	}
	
	@Test
	public void testFunctionExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(e);
		
		continuationCoordinator.execute(documentScriptEnvironment, function);
		
		verify(logger).error(unexpectedExceptionString, documentScriptEnvironment);
		verify(logger).error("", e);
	}
	
	@Test
	public void testContinuationResumptionNoContinuation() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		boolean result = continuationCoordinator.resumeContinuation(documentScriptEnvironment, pendingKey, args);
		
		assertThat(result, is(true));
	}
	
	@Test
	public void testContinuationResumptionWithContinuation() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(continuation);
		
		given(continuationState.type()).willReturn(ContinuationType.RequiredModule);
		
		boolean result = continuationCoordinator.resumeContinuation(documentScriptEnvironment, pendingKey, args);
		
		assertThat(result, is(false));
		verify(continuationProcessor3).process(continuationState);
	}
	
	@Test
	public void testContinuationResumptionWithUnexpectedException() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(e);
		
		continuationCoordinator.resumeContinuation(documentScriptEnvironment, pendingKey, args);
		
		verify(logger).error(unexpectedExceptionString, documentScriptEnvironment);
		verify(logger).error("", e);
	}

}
