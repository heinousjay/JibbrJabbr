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

import jj.engine.EngineAPI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.ConstProperties;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.RhinoException;
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
	
	@Mock Context context;
	
	@Mock Script script;
	
	@Mock ScriptBundle scriptBundle;
	
	@Mock AssociatedScriptBundle associatedScriptBundle;
	
	@Mock EngineAPI engineAPI;
	
	@Mock CurrentScriptContext currentScriptContext;
	
	@Mock ContinuationPending continuation;
	
	@Mock ContinuationState continuationState;
	
	@Mock Logger logger;
	
	@Captor ArgumentCaptor<StringBuilder> rhinoLogCaptor;
	
	@Captor ArgumentCaptor<String> logCaptor;
	
	@Mock Callable function;

	@InjectMocks ContinuationCoordinator continuationCoordinator;
	
	final Object[] args = { new Object(), new Object() };
	
	final RhinoException rhinoException;
	
	final String rhinoExceptionString = "trouble executing a script\n============== BEGIN SCRIPT TRACE ==============\nmissing ; before statement (#1)\n==============  END SCRIPT TRACE  ==============\n";
	
	final String unexpectedExceptionString = "unexpected problem during script execution {}";
	
	public ContinuationCoordinatorTest() {
		RhinoException caught = null;
		Context context = Context.enter();
		try {
			context.evaluateString(context.initStandardObjects(), "not really javascript", "", 1, null);
		} catch (RhinoException re) {
			caught = re;
		} finally {
			Context.exit();
		}
		rhinoException = caught;
	}
	
	@Before
	public void before() {
		
		given(scriptBundle.script()).willReturn(script);
		given(scriptBundle.scope()).willReturn(scope);
		given(scriptBundle.sha1()).willReturn(sha1);
		
		given(associatedScriptBundle.script()).willReturn(script);
		given(associatedScriptBundle.scope()).willReturn(scope);
		given(associatedScriptBundle.sha1()).willReturn(sha1);
		
		given(engineAPI.context()).willReturn(context);
		
		given(continuation.getApplicationState()).willReturn(continuationState);
		
		Context.enter();
	}
	
	@Test
	public void testInitialExecutionNoContinuation() {
		
		ContinuationState result = continuationCoordinator.execute(scriptBundle);
		
		verify((ConstProperties)scope).putConst("scriptKey", scope, sha1);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testInitialExecutionWithContinuation() {
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(continuation);
		
		ContinuationState result = continuationCoordinator.execute(scriptBundle);

		assertThat(result, is(continuationState));
	}
	
	@Test
	public void testInitialExecutionWithRhinoException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(rhinoException);
		
		continuationCoordinator.execute(scriptBundle);
		
		verify(logger).error(eq("{}"), rhinoLogCaptor.capture());
		
		assertThat(rhinoLogCaptor.getValue().toString(), is(rhinoExceptionString));
	}
	
	@Test
	public void testInitialExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(e);
		
		continuationCoordinator.execute(scriptBundle);
		
		verify(logger).error(logCaptor.capture(), eq(scriptBundle));
		verify(logger).error("", e);
		
		assertThat(logCaptor.getValue(), is(unexpectedExceptionString));
	}
	
	@Test
	public void testFunctionExecutionNoContinuation() {
		
		ContinuationState result = continuationCoordinator.execute(associatedScriptBundle, function, args[0], args[1]);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testFunctionExecutionWithContinutation() {
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(continuation);
		
		ContinuationState result = continuationCoordinator.execute(associatedScriptBundle, function, args);
		
		assertThat(result, is(continuationState));
	}
	
	@Test
	public void testFunctionExecutionWithRhinoException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(rhinoException);
		
		continuationCoordinator.execute(associatedScriptBundle, function);
		
		verify(logger).error(eq("{}"), rhinoLogCaptor.capture());
		
		assertThat(rhinoLogCaptor.getValue().toString(), is(rhinoExceptionString));
	}
	
	@Test
	public void testFunctionExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(e);
		
		continuationCoordinator.execute(associatedScriptBundle, function);
		
		verify(logger).error(logCaptor.capture(), eq(associatedScriptBundle));
		verify(logger).error("", e);
		
		assertThat(logCaptor.getValue(), is(unexpectedExceptionString));
	}
	
	@Test
	public void testContinuationResumptionNoContinuation() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		ContinuationState result = continuationCoordinator.resumeContinuation(pendingKey, associatedScriptBundle, args);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void testContinuationResumptionWithContinutation() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(continuation);
		
		ContinuationState result = continuationCoordinator.resumeContinuation(pendingKey, associatedScriptBundle, args);
		
		assertThat(result, is(continuationState));
	}
	
	@Test
	public void testContinuationResumptionWithRhinoException() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(rhinoException);
		
		continuationCoordinator.resumeContinuation(pendingKey, associatedScriptBundle, args);
		
		verify(logger).error(eq("{}"), rhinoLogCaptor.capture());
		
		assertThat(rhinoLogCaptor.getValue().toString(), is(rhinoExceptionString));
	}
	
	@Test
	public void testContinuationResumptionWithUnexpectedException() {
		
		given(currentScriptContext.pendingContinuation(pendingKey)).willReturn(continuation);
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(e);
		
		continuationCoordinator.resumeContinuation(pendingKey, associatedScriptBundle, args);
		
		verify(logger).error(logCaptor.capture(), eq(associatedScriptBundle));
		verify(logger).error("", e);
		
		assertThat(logCaptor.getValue(), is(unexpectedExceptionString));
	}

}
