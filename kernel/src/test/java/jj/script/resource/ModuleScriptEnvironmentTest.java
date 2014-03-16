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
package jj.script.resource;


import static jj.configuration.AppLocation.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

import java.net.URI;

import jj.engine.EngineAPI;
import jj.resource.ResourceCacheKey;
import jj.resource.ResourceFinder;
import jj.script.ContinuationPendingKey;
import jj.script.RealRhinoContextProvider;
import jj.script.AbstractScriptEnvironment;
import jj.script.RhinoContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mozilla.javascript.ScriptableObject;

/**
 * @author jason
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ModuleScriptEnvironmentTest {
	
	@Mock ResourceFinder resourceFinder;
	@Mock InjectorBridgeFunction injectorBridge;
	@Mock EngineAPI api;
	RealRhinoContextProvider contextProvider;
	
	String moduleIdentifier;
	RequiredModule requiredModule;
	
	@Mock AbstractScriptEnvironment parent;
	
	@Mock ScriptResource scriptResource;
	
	ModuleScriptEnvironment mse;

	@Before
	public void before() {
	}
	
	@Test
	public void test() {
		
	}

}
