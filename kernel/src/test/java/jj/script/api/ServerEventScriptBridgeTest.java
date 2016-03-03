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
package jj.script.api;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static jj.script.api.ServerEventScriptResult.*;

import java.lang.reflect.Method;

import jj.event.Listener;
import jj.event.Subscriber;
import jj.execution.MockTaskRunner;
import jj.execution.TaskRunner;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;
import jj.script.ScriptEnvironmentDied;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Callable;

import com.google.inject.Injector;

/**
 * fairly ugly test.  but hey! code generation is ugly
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerEventScriptBridgeTest {

	@Mock CurrentScriptEnvironment env;
	@Mock Injector injector;
	
	@InjectMocks ServerEventScriptBridge sesb;
	
	@Mock ScriptEnvironment<?> se;
	@Mock Callable callable1;
	@Mock Callable callable2;
	
	MockTaskRunner taskRunner;
	
	@Mock ServerEventCallableInvoker seci;
	
	@Mock ScriptEnvironmentDied sed;
	
	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		
		willReturn(se).given(env).current();
		// this is really just to return something
		// javac seems to only like this method.  annoying!
		given(injector.getInstance((Class<? extends ServerEventCallableInvoker>)any(Class.class))).willAnswer(i -> seci);
	}
	
	@Test
	public void testBasicFlow() throws Exception {
		
		assertThat(sesb.subscribe("not an event", callable1), is(NotAnEventClass));
		
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable1), is(Success));
		
		// check that the instance is set up
		verify(seci).invocationInstances(se, callable1);
		
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable1), is(AlreadyBound));
		
		assertThat(sesb.unsubscribe("jj.script.ScriptEnvironmentDied", callable1), is(Success));
		
		verify(seci).kill();
		
		assertThat(sesb.unsubscribe("jj.script.ScriptEnvironmentDied", callable1), is(NotBound));
		
		// in a different method for organization purposes but
		// we call it from here because we have affirmatively
		// generated the class we care about at this point
		testGeneratedClass();
	}
	
	private void testGeneratedClass() throws Exception {
		
		@SuppressWarnings("unchecked")
		Class<? extends ServerEventCallableInvoker> generated = 
			(Class<? extends ServerEventCallableInvoker>) Class.forName("jj.script.api.GeneratedInvokerFor$$jj_script_ScriptEnvironmentDied");
		
		assertThat(generated.getAnnotation(Subscriber.class), is(notNullValue()));
		
		Method bridge = generated.getDeclaredMethod("invocationBridge", ScriptEnvironmentDied.class);
		
		assertThat(bridge.getAnnotation(Listener.class), is(notNullValue()));
		
		
		// and now we can test the actual instance
		ServerEventCallableInvoker instance = generated
			.getConstructor(TaskRunner.class)
			.newInstance(taskRunner);
		
		instance.invocationInstances(se, callable1);
		
		bridge.invoke(instance, sed);
		
		taskRunner.runFirstTask();
		
		verify(se).execute(callable1, sed);
		
		instance.kill();
		
		bridge.invoke(instance, sed);
		
		assertThat(taskRunner.tasks, is(empty()));
	}
	
	@Test
	public void testMultipleCallables() throws Exception {

		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable1), is(Success));
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable2), is(Success));
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable1), is(AlreadyBound));
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable2), is(AlreadyBound));
		assertThat(sesb.unsubscribe("jj.script.ScriptEnvironmentDied", callable1), is(Success));
		assertThat(sesb.unsubscribe("jj.script.ScriptEnvironmentDied", callable2), is(Success));
		assertThat(sesb.unsubscribe("jj.script.ScriptEnvironmentDied", callable1), is(NotBound));
		assertThat(sesb.unsubscribe("jj.script.ScriptEnvironmentDied", callable2), is(NotBound));
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable1), is(Success));
		assertThat(sesb.subscribe("jj.script.ScriptEnvironmentDied", callable2), is(Success));
	}

}
