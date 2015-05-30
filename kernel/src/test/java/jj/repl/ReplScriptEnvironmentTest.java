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
package jj.repl;

import static jj.server.ServerLocation.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jj.script.MockAbstractScriptEnvironmentDependencies;
import jj.script.RealRhinoContextProvider;
import jj.script.RhinoContext;
import jj.script.module.ScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplScriptEnvironmentTest {

	RealRhinoContextProvider contextProvider;
	MockAbstractScriptEnvironmentDependencies dependencies;
	
	ReplScriptEnvironment rse;
	
	@Mock ScriptResource script;
	
	@Before
	public void before() {
		contextProvider = new RealRhinoContextProvider();
		dependencies = new MockAbstractScriptEnvironmentDependencies(contextProvider, ReplScriptEnvironment.NAME);
		
		given(dependencies.resourceFinder().loadResource(ScriptResource.class, Assets, ReplScriptEnvironment.BASE_REPL_SYSTEM)).willReturn(script);
		
		try (RhinoContext context = contextProvider.get()) {
			rse = new ReplScriptEnvironment(dependencies, context.initStandardObjects(), new CurrentReplChannelHandlerContext());
		}
	}
	
	@Test
	public void test() {
		
		Set<String> keys = new HashSet<>(Arrays.asList(
			"module",
			"exports",
			"require",
			"setInterval",
			"setTimeout",
			"clearInterval",
			"clearTimeout",
			"inject"
		));
		
		for (Object id : rse.scope().getIds()) {
			keys.remove(id);
		}
		
		assertTrue(keys.isEmpty());
		
		// not sure what else to check here, this is really as far as it goes for now
	}

}
