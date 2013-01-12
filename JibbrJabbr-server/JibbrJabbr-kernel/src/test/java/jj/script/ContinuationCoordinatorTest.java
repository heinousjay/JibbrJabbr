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
import jj.hostapi.RhinoObjectCreator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
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
	
	Scriptable scope;
	
	@Mock ScriptBundle scriptBundle;
	
	@Mock RhinoObjectCreator rhinoObjectCreator;
	
	@Mock CurrentScriptContext currentScriptContext;

	ContinuationCoordinator continuationCoordinator;
	
	@Before
	public void before() {
		
		scope = new NativeObject();
		
		given(scriptBundle.scope()).willReturn(scope);
		given(scriptBundle.sha1()).willReturn(sha1);
		
		given(rhinoObjectCreator.context()).will(new Answer<Context>() {

			@Override
			public Context answer(InvocationOnMock invocation) throws Throwable {
				Context context = Context.enter();
				context.setOptimizationLevel(-1);
				context.setLanguageVersion(Context.VERSION_1_8);
				return context;
			}
		});
		
		continuationCoordinator = new ContinuationCoordinator(rhinoObjectCreator, currentScriptContext);
	}
	
	@Test
	public void test() {
		
		Script script = rhinoObjectCreator.context().compileString("(function() { return scriptKey;})();", "", 1, null);
		Context.exit();
		given(scriptBundle.script()).willReturn(script);
		
		ContinuationState result = continuationCoordinator.execute(scriptBundle);
		
		assertThat(result, is(nullValue()));
	}

}
