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
package jj.i18n;

import static jj.server.ServerLocation.Virtual;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Locale;

import jj.execution.MockTaskRunner;
import jj.i18n.ScriptMessages.ScriptMessagesLoaderBundle;
import jj.resource.ResourceFinder;
import jj.script.PendingKey;
import jj.script.ContinuationState;
import jj.script.CurrentScriptEnvironment;
import jj.script.ScriptEnvironment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ScriptMessagesTest {
	
	String name = "name";
	
	@Mock ResourceFinder resourceFinder;
	MockTaskRunner taskRunner;
	@Mock CurrentScriptEnvironment env;
	@Mock I18NConfiguration configuration;

	ScriptMessages sm;
	
	@Mock ScriptEnvironment<?> se;
	@Mock Scriptable scope;
	
	@Mock MessagesResource mr;
	
	@Captor ArgumentCaptor<ScriptMessagesLoaderBundle> bundleCaptor;
	@Mock PendingKey pendingKey;
	@Mock ContinuationState continuationState;
	@Captor ArgumentCaptor<Scriptable> scriptableCaptor;
	@Captor ArgumentCaptor<Undefined> undefinedCaptor;

	@Before
	public void before() {
		taskRunner = new MockTaskRunner();
		
		sm = new ScriptMessages(resourceFinder, taskRunner, env, configuration);
		
		willReturn(se).given(env).current();
		given(se.scope()).willReturn(scope);
		
		given(mr.containsKey("key")).willReturn(true);
		given(mr.message("key")).willReturn("value");
	}
	
	@Test
	public void testRequiresActiveScript() {
		given(env.current()).willReturn(null);
		
		boolean failed = false;
		try {
			sm.getMessagesResource("", "");
			failed = true;
		} catch (AssertionError ae) {}
		
		assertFalse(failed);
	}
	
	@Test
	public void testFoundMessagesResourceWithGivenNameAndGivenLocale() {
		
		Locale l = Locale.US;

		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, l)).willReturn(mr);
		
		MessagesScriptable s = (MessagesScriptable)sm.getMessagesResource(name, l.toLanguageTag());
		
		// well, it had better be right!
		// easiest check
		assertThat(s.get("key", null), is("value"));
		
	}
	
	@Test
	public void testFoundMessagesResourceWithGivenNameAndConfiguredLocale() {
		
		// not a real locale, but it'll take it and it will never be a default no matter who runs the test
		Locale l = Locale.forLanguageTag("xx-YY");
		
		
		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, l)).willReturn(mr);
		given(configuration.defaultLocale()).willReturn(l);
		
		MessagesScriptable s = (MessagesScriptable)sm.getMessagesResource(name, "");
		
		assertThat(s.get("key", null), is("value"));
	}
	
	@Test
	public void testFoundMessageWithGivenNameAndDefaultLocale() {
		
		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, Locale.getDefault())).willReturn(mr);
		
		MessagesScriptable s = (MessagesScriptable)sm.getMessagesResource(name, "");
		
		assertThat(s.get("key", null), is("value"));
	}
	
	@Test
	public void testFoundMessagesResourceWithNoNameAndGivenLocale() {
		
		Locale l = Locale.US;

		given(se.name()).willReturn(name);
		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, l)).willReturn(mr);
		
		MessagesScriptable s = (MessagesScriptable)sm.getMessagesResource("", l.toLanguageTag());
		
		// well, it had better be right!
		// easiest check
		assertThat(s.get("key", null), is("value"));
		
	}
	
	@Test
	public void testFoundMessagesResourceWithNoNameAndConfiguredLocale() {
		
		// not a real locale, but it'll take it and it will never be a default no matter who runs the test
		Locale l = Locale.forLanguageTag("xx-YY");
		

		given(se.name()).willReturn(name);
		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, l)).willReturn(mr);
		given(configuration.defaultLocale()).willReturn(l);
		
		MessagesScriptable s = (MessagesScriptable)sm.getMessagesResource("", "");
		
		assertThat(s.get("key", null), is("value"));
	}
	
	@Test
	public void testFoundMessageWithNoNameAndDefaultLocale() {

		given(se.name()).willReturn(name);
		given(resourceFinder.findResource(MessagesResource.class, Virtual, name, Locale.getDefault())).willReturn(mr);
		
		MessagesScriptable s = (MessagesScriptable)sm.getMessagesResource("", "");
		
		assertThat(s.get("key", null), is("value"));
	}

	// checks the continuation processing part of this business
	@Test
	public void testLoadedMessagesResource() throws Exception {
		
		Locale l = Locale.US;
		RuntimeException re = new RuntimeException();

		given(env.preparedContinuation(any(ScriptMessagesLoaderBundle.class))).willThrow(re);
		given(resourceFinder.loadResource(MessagesResource.class, Virtual, name, l)).willReturn(mr);
		
		try {
			sm.getMessagesResource(name, l.toLanguageTag());
			fail("should have thrown");
		} catch (RuntimeException e) {
			assertThat(e, is(sameInstance(re)));
		}
		
		verify(env).preparedContinuation(bundleCaptor.capture());
		
		ScriptMessagesLoaderBundle bundle = bundleCaptor.getValue();
		bundle.pendingKey(pendingKey);
		given(continuationState.continuationAs(ScriptMessagesLoaderBundle.class)).willReturn(bundle);
		sm.process(continuationState);
		taskRunner.runFirstTask();
		
		verify(pendingKey).resume(scriptableCaptor.capture());
		
		assertThat(scriptableCaptor.getValue().get("key", null), is("value"));
	}
	
	// just validates that if the resources are not found it restarts with Undefined
	@Test
	public void testNotFoundMessagesResource() throws Exception {
		ScriptMessagesLoaderBundle bundle = new ScriptMessagesLoaderBundle(name, null, null);
		bundle.pendingKey(pendingKey);
		given(continuationState.continuationAs(ScriptMessagesLoaderBundle.class)).willReturn(bundle);
		sm.process(continuationState);
		taskRunner.runFirstTask();
		
		verify(pendingKey).resume(undefinedCaptor.capture());
		
		assertThat(undefinedCaptor.getValue(), is(Undefined.instance));
	}
}
