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
package jj.api;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;

import java.util.HashSet;
import java.util.Set;

import jj.logging.EmergencyLog;
import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */


@RunWith(MockitoJUnitRunner.class)
public class APIImplTest {
	
	RealRhinoContextProvider contextProvider;
	@Mock RequireFunction requireFunction;
	@Mock EmergencyLog logger;
	@Mock APIContributor apiContributor1;
	@Mock APIContributor apiContributor2;
	Set<APIContributor> apiContributors;
	
	API apiImpl;
	
	@SafeVarargs
	public static <T> Set<T> setOf(T...items) {
		Set<T> result = new HashSet<>();
		for (T item : items) {
			result.add(item);
		}
		return result;
	}
	
	@Before
	public void before() {
	}
	
	@Test
	public void test() {

		contextProvider = new RealRhinoContextProvider();
		
		apiContributors = setOf(apiContributor1, apiContributor2);
		
		given(requireFunction.call(
			isA(Context.class),
			isA(Scriptable.class),
			isA(Scriptable.class),
			isA(Object[].class))
		).willReturn(new Object());
		
		apiImpl = new API(contextProvider, requireFunction, logger, apiContributors);
		
		ScriptableObject global = apiImpl.global();
		
		try (RhinoContext context = contextProvider.get()) {
			
			// indirectly verifies global is set up as desired
			assertThat((Boolean)context.evaluateString(global, "/hi/.test('hi');", "APIImplTest - RegExp is available"), is(true));
			
			context.evaluateString(global, "global['//makeRequire']({id:'base'})('id')", "APIImplTest - //makeRequire");
			
			verify(requireFunction).call(
				isA(Context.class),
				isA(Scriptable.class),
				isA(Scriptable.class),
				eq(new Object[]{"id", "base"})
			);
			
			Object result = 
				context.evaluateString(
					global, 
					"(function() {return global['" + API.PROP_CONVERT_ARGS + "'](arguments);})('hi',3,{key:['value',1]})",
					"APIImplTest - " + API.PROP_CONVERT_ARGS
				);
			
			assertThat(result, is(instanceOf(String.class)));
			assertThat((String)result, is("[\"hi\",3,{\"key\":[\"value\",1]}]"));
		}
		
		verify(apiContributor1).contribute(eq(global), isA(RhinoContext.class));
		verify(apiContributor2).contribute(eq(global), isA(RhinoContext.class));
	}

}
