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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
	
	@Mock ScriptExecutionEnvironment scriptExecutionEnvironment;
	
	@Mock DocumentScriptExecutionEnvironment associatedScriptExecutionEnvironment;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ContinuationPending continuation;
	
	@Mock ContinuationState continuationState;
	
	@Mock Logger logger;
	
	@Captor ArgumentCaptor<String> logCaptor;
	
	@Mock Callable function;

	ContinuationCoordinator continuationCoordinator;
	
	final Object[] args = { new Object(), new Object() };
	
	final String unexpectedExceptionString = "unexpected problem during script execution {}";
	
	@Before
	public void before() {
		
		given(scriptExecutionEnvironment.script()).willReturn(script);
		given(scriptExecutionEnvironment.scope()).willReturn(scope);
		given(scriptExecutionEnvironment.sha1()).willReturn(sha1);
		
		given(associatedScriptExecutionEnvironment.script()).willReturn(script);
		given(associatedScriptExecutionEnvironment.scope()).willReturn(scope);
		given(associatedScriptExecutionEnvironment.sha1()).willReturn(sha1);
		
		given(continuation.getApplicationState()).willReturn(continuationState);
		
		RhinoContextMaker contextMaker = new MockRhinoContextMaker();
		context = contextMaker.context();
		continuationCoordinator = new ContinuationCoordinator(contextMaker, currentScriptContext, logger);
	}
	
	@Test
	public void testInitialExecutionNoContinuation() {
		
		ContinuationState result = continuationCoordinator.execute(scriptExecutionEnvironment);
		
		verify((ConstProperties)scope).putConst("scriptKey", scope, sha1);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testInitialExecutionWithContinuation() {
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(continuation);
		
		ContinuationState result = continuationCoordinator.execute(scriptExecutionEnvironment);

		assertThat(result, is(continuationState));
	}
	
	@Test
	public void testInitialExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(e);
		
		continuationCoordinator.execute(scriptExecutionEnvironment);
		
		verify(logger).error(logCaptor.capture(), eq(scriptExecutionEnvironment));
		verify(logger).error("", e);
		
		assertThat(logCaptor.getValue(), is(unexpectedExceptionString));
	}
	
	@Test
	public void testFunctionExecutionNoContinuation() {
		
		ContinuationState result = continuationCoordinator.execute(associatedScriptExecutionEnvironment, function, args[0], args[1]);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testFunctionExecutionWithContinutation() {
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(continuation);
		
		ContinuationState result = continuationCoordinator.execute(associatedScriptExecutionEnvironment, function, args);
		
		assertThat(result, is(continuationState));
	}
	
	@Test
	public void testFunctionExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(e);
		
		continuationCoordinator.execute(associatedScriptExecutionEnvironment, function);
		
		verify(logger).error(logCaptor.capture(), eq(associatedScriptExecutionEnvironment));
		verify(logger).error("", e);
		
		assertThat(logCaptor.getValue(), is(unexpectedExceptionString));
	}
	
	@Test
	public void testContinuationResumptionNoContinuation() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		ContinuationState result = continuationCoordinator.resumeContinuation(pendingKey, associatedScriptExecutionEnvironment, args);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testContinuationResumptionWithContinutation() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(continuation);
		
		ContinuationState result = continuationCoordinator.resumeContinuation(pendingKey, associatedScriptExecutionEnvironment, args);
		
		assertThat(result, is(continuationState));
	}
	
	@Test
	public void testContinuationResumptionWithUnexpectedException() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(e);
		
		continuationCoordinator.resumeContinuation(pendingKey, associatedScriptExecutionEnvironment, args);
		
		verify(logger).error(logCaptor.capture(), eq(associatedScriptExecutionEnvironment));
		verify(logger).error("", e);
		
		assertThat(logCaptor.getValue(), is(unexpectedExceptionString));
	}

}
