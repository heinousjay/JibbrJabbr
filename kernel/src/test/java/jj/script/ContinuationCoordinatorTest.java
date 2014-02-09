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

import java.util.HashMap;
import java.util.Map;

import jj.Closer;
import jj.jjmessage.JJMessage;

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
	
	@Mock AbstractScriptEnvironment scriptEnvironment;
	
	@Mock CurrentScriptEnvironment env;
	@Mock Closer closer;
	
	@Mock ContinuationPending continuation;
	
	ContinuationState continuationState;
	
	@Mock Logger logger;
	
	@Mock Callable function;

	ContinuationCoordinatorImpl continuationCoordinator;
	
	final Object[] args = { new Object(), new Object() };
	
	final String unexpectedExceptionString = "unexpected problem during script execution {}";
	
	@Mock ContinuationProcessor continuationProcessor1;
	
	@Mock ContinuationProcessor continuationProcessor2;
	
	@Mock ContinuationProcessor continuationProcessor3;
	
	@Before
	public void before() {
		
		given(env.enterScope(any(ScriptEnvironment.class))).willReturn(closer);
		
		given(scriptEnvironment.script()).willReturn(script);
		given(scriptEnvironment.scope()).willReturn(scope);
		given(scriptEnvironment.sha1()).willReturn(sha1);
		
		continuationState = new ContinuationState(JJMessage.makeRetrieve(""));
		given(continuation.getApplicationState()).willReturn(continuationState);
		
		Map<Class<? extends Continuable>, ContinuationProcessor> continuationProcessors = new HashMap<>();
		continuationProcessors.put(RestRequest.class, continuationProcessor1);
		continuationProcessors.put(JJMessage.class, continuationProcessor2);
		continuationProcessors.put(RequiredModule.class, continuationProcessor3);
		
		MockRhinoContextProvider contextProvider = new MockRhinoContextProvider();
		context = contextProvider.get();
		continuationCoordinator = new ContinuationCoordinatorImpl(contextProvider, env, logger, continuationProcessors);
	}
	
	public void verifyDefaultContextMaintained() {
		// the continuation coordinator is responsible for maining the scope
		// of the script environment
		verify(env).enterScope(scriptEnvironment);
		assertThat(env.current(), is(nullValue()));
	}
	
	public void verifyContinuedContextMaintained() {
		// the continuation coordinator is responsible for maining the scope
		// of the script environment
		verify(env).enterScope(eq(scriptEnvironment), anyString());
		assertThat(env.current(), is(nullValue()));
	}
	
	@Test
	public void testInitialExecutionNoContinuation() {
		
		boolean result = continuationCoordinator.execute(scriptEnvironment);
		
		assertThat(result, is(true));
		verifyDefaultContextMaintained();
	}
	
	@Test
	public void testInitialExecutionWithContinuation() {
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(continuation);
		
		boolean result = continuationCoordinator.execute(scriptEnvironment);

		assertThat(result, is(false));
		verify(continuationProcessor2).process(continuationState);
		verifyDefaultContextMaintained();
	}
	
	@Test
	public void testInitialExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.executeScriptWithContinuations(script, scope)).willThrow(e);
		
		continuationCoordinator.execute(scriptEnvironment);
		
		verify(logger).error(unexpectedExceptionString, scriptEnvironment);
		verify(logger).error("", e);
		verifyDefaultContextMaintained();
	}
	
	@Test
	public void testFunctionExecutionNoContinuation() {
		
		boolean result = continuationCoordinator.execute(scriptEnvironment, function, args[0], args[1]);
		
		assertThat(result, is(true));
		verifyDefaultContextMaintained();
	}
	
	@Test
	public void testFunctionExecutionWithContinuation() {
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(continuation);
		
		boolean result = continuationCoordinator.execute(scriptEnvironment, function, args);
		
		assertThat(result, is(false));
		verify(continuationProcessor2).process(continuationState);
		verifyDefaultContextMaintained();
	}
	
	@Test
	public void testFunctionExecutionWithUnexpectedException() {
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.callFunctionWithContinuations(eq(function), eq(scope), any(Object[].class))).willThrow(e);
		
		continuationCoordinator.execute(scriptEnvironment, function);
		
		verify(logger).error(unexpectedExceptionString, scriptEnvironment);
		verify(logger).error("", e);
		verifyDefaultContextMaintained();
	}
	
	@Test
	public void testContinuationResumptionNoContinuation() {
		
		given(scriptEnvironment.continuationPending(pendingKey)).willReturn(continuation);
		
		boolean result = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, args);
		
		assertThat(result, is(true));
	}
	
	@Test
	public void testContinuationResumptionWithContinuation() {
		
		given(scriptEnvironment.continuationPending(pendingKey)).willReturn(continuation);
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(continuation);
		
		boolean result = continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, args);
		
		assertThat(result, is(false));
		verify(continuationProcessor2).process(continuationState);
		verifyContinuedContextMaintained();
	}
	
	@Test
	public void testContinuationResumptionWithUnexpectedException() {
		
		given(scriptEnvironment.continuationPending(pendingKey)).willReturn(continuation);
		
		given(logger.isErrorEnabled()).willReturn(true);
		
		final RuntimeException e = new RuntimeException();
		
		given(context.resumeContinuation(any(), eq(scope), eq(args))).willThrow(e);
		
		continuationCoordinator.resumeContinuation(scriptEnvironment, pendingKey, args);
		
		verify(logger).error(unexpectedExceptionString, scriptEnvironment);
		verify(logger).error("", e);
		verifyContinuedContextMaintained();
	}

}
